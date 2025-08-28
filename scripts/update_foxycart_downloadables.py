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

def confirm_token_ok(token):
    print("Confirm token ok")
    resp = requests.get(
        FOXYCART_ROOT_URL,
        headers={
            "FOXY-API-VERSION": "1",
            "Authorization": f"Bearer {token}",
            "Accept": "application/json",
        },
    )
    print("reply:", resp.status_code, resp.text)
    return None

def get_downloadables(token):
    downloadables = []
    resp = requests.get(
        f"{FOXYCART_STORE_BASE}/downloadables",
        headers={
            "FOXY-API-VERSION": "1",
            "Authorization": f"Bearer {token}",
            "Accept": "application/json",
        },
    )
    print("token:", token)
    print("Status code:", resp.status_code)
    print("Response body:", resp.text)
    # while url:
    #     resp.raise_for_status()
    #     data = resp.json()
    #     downloadables.extend(data.get("_embedded", {}).get("fx:downloadables", []))
    #     url = data.get("_links", {}).get("next", {}).get("href")
    return downloadables

def extract_os_key(asset_name):
    # Matches Makelangelo-[os]-[date].[ext], case-insensitive, [os] may have dashes
    m = re.match(r"(?i)makelangelo-(.+)-\d{8}\.[^.]+$", asset_name)
    if m:
        return m.group(1).lower()
    return None

def match_downloadable(asset_name, downloadables):
    os_key = extract_os_key(asset_name)
    if not os_key:
        print(f"Could not extract OS key from asset name: {asset_name}")
        return None
    expected_code = f"soft-001{os_key}".lower()
    for d in downloadables:
        code = d.get("code", "").lower()
        if code == expected_code:
            return d
    return None

def update_downloadable(token, downloadable, asset_path):
    upload_url = downloadable["_links"]["fx:file"]["href"]
    headers = {
        "FOXY-API-VERSION": "1",
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/octet-stream",
        "Accept": "application/json",
    }
    with open(asset_path, "rb") as f:
        resp = requests.put(upload_url, headers=headers, data=f)
    resp.raise_for_status()
    print(f"Updated {downloadable.get('name')} ({downloadable.get('code')}) with {asset_path}")

def main():
    try:
        token = get_access_token()
    except Exception as e:
        print("Failed to obtain FoxyCart access token:", e)
        sys.exit(1)

    try:
        confirm_token_ok(token)
    except Exception as e:
        print("FoxyCart access token is not valid:", e)
        sys.exit(1)

    try:
        downloadables = get_downloadables(token)
    except Exception as e:
        print("Failed to get FoxyCart downloadables:", e)
        sys.exit(1)

    assets = glob.glob("Makelangelo-*-*-*.*")
    if not assets:
        print("No Makelangelo-*-*-*.* assets found in current directory.")
        sys.exit(0)

    matched, unmatched = 0, 0
    for asset_path in assets:
        asset_name = os.path.basename(asset_path)
        downloadable = match_downloadable(asset_name, downloadables)
        if downloadable:
            try:
                update_downloadable(token, downloadable, asset_path)
                matched += 1
            except Exception as e:
                print(f"Error updating {asset_name}: {e}")
        else:
            print(f"No FoxyCart downloadable matched for asset {asset_name}")
            unmatched += 1

    print(f"Done. {matched} assets updated. {unmatched} assets unmatched.")

if __name__ == "__main__":
    main()