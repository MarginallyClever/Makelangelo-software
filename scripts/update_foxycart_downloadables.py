#
# This script updates FoxyCart downloadables with local files matching
# the pattern Makelangelo-[os]-[date].[ext] in the current directory.
# It uses environment variables for FoxyCart API credentials.
#

import os
import requests
import base64
import glob
import sys
import re

STORE_ID = os.environ.get("FOXYCART_STORE_ID", "53596")
FOXYCART_ROOT_URL = "https://api.foxycart.com"
FOXYCART_TOKEN_URL = f"{FOXYCART_ROOT_URL}/token"
FOXYCART_STORE_BASE = f"{FOXYCART_ROOT_URL}/stores/{STORE_ID}"
ASSET_DIR = os.environ.get("RELEASE_ASSETS_DIR", ".").rstrip("/")

def get_access_token():
    client_id = os.environ["FOXYCART_CLIENT_ID"]
    client_secret = os.environ["FOXYCART_CLIENT_SECRET"]
    auth_str = f"{client_id}:{client_secret}"
    b64_auth = base64.b64encode(auth_str.encode()).decode()

    resp = requests.post(
        FOXYCART_TOKEN_URL,
        data={
            "grant_type": "refresh_token",
            "refresh_token": os.environ["FOXYCART_REFRESH_TOKEN"],
        },
        headers={
            "Authorization": f"Basic {b64_auth}",
            "Content-Type": "application/x-www-form-urlencoded"
        },
    )
    print("Status code:", resp.status_code)
    print("Response body:", resp.text)
    resp.raise_for_status()
    return resp.json()["access_token"]

def get_downloadables(token):
    resp = requests.get(
        f"{FOXYCART_STORE_BASE}/downloadables",
        headers={
            "FOXY-API-VERSION": "1",
            "Authorization": f"Bearer {token}",
            "Accept": "application/json",
        },
    )
    resp.raise_for_status()
    data = resp.json()
    downloadables = data.get("_embedded", {}).get("fx:downloadables", [])
    print("downloadables:",len(downloadables))
    return downloadables

def extract_os_key(asset_name):
    # Matches Makelangelo-[os]-[date].[ext], case-insensitive, [os] may have dashes
    m = re.match(r"(?i)makelangelo-(.+)-\d{8}\.[^.]+$", asset_name)
    if m:
        return m.group(1).lower()
    return None

def match_downloadable(asset_name, downloadables):
    os_key = extract_os_key(asset_name)
    print(f"Matching downloadable: {asset_name} produces OS key: {os_key}")
    if not os_key:
        print(f"Could not extract OS key from asset name: {asset_name}")
        return None
    expected_code = f"SOFT-0001{os_key}".lower()
    print(f"Matching expected code {expected_code}")
    for d in downloadables:
        code = d.get("code", "").lower()
        if code == expected_code:
            return d
    return None

def update_downloadable(token, downloadable, asset_path):
    upload_url = downloadable["_links"]["self"]["href"]
    headers = {
        "FOXY-API-VERSION": "1",
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/octet-stream",
        "Accept": "application/json",
    }
    with open(asset_path, "rb") as f:
        resp = requests.patch(upload_url, headers=headers, data=f)
    resp.raise_for_status()
    print(f"Updated {downloadable.get('name')} ({downloadable.get('code')}) with {asset_path}")

def main():
    try:
        token = get_access_token()
    except Exception as e:
        print("Failed to obtain FoxyCart access token:", e)
        sys.exit(1)

    try:
        downloadables = get_downloadables(token)
    except Exception as e:
        print("Failed to get FoxyCart downloadables:", e)
        sys.exit(1)

    pattern = os.path.join(ASSET_DIR, "Makelangelo-*.*")
    assets = glob.glob(pattern)
    print(f"Scanning {ASSET_DIR} -> {len(assets)} candidate files")
    if not assets:
        print("No matching Makelangelo assets.")
        # List directory for diagnostics
        print("Directory listing:")
        for entry in os.listdir(ASSET_DIR):
            print(" -", entry)
        sys.exit(1)

    matched, unmatched = 0, 0
    for asset_path in assets:
        asset_name = os.path.basename(asset_path)
        found = match_downloadable(asset_name, downloadables)
        if found:
            try:
                update_downloadable(token, found, asset_path)
                matched += 1
            except Exception as e:
                print(f"Error updating {asset_name}: {e}")
        else:
            print(f"No FoxyCart downloadable matched for asset {asset_name}")
            unmatched += 1

    print(f"Done. {matched} assets updated. {unmatched} assets unmatched.")

if __name__ == "__main__":
    main()