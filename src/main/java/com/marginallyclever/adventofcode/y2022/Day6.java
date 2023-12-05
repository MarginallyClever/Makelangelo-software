package com.marginallyclever.adventofcode.y2022;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Stack;

public class Day6 {
    private static JSONObject root = new JSONObject();
    private static Stack<JSONObject> stack = new Stack<>();

    private static int totalSum=0;
    private static int diskSize = 70000000;
    private static int needed = 30000000;

    public static void main(String[] args) {
        stack.push(root);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(Day6.class.getResourceAsStream("input.txt"))))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(br,line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //printTree(root,0);
        names.push("");
        int totalUsed = treeSize(root,0);
        System.out.println("total size: "+totalSum);

        int freeSpace = diskSize - totalUsed;
        needed = 30000000 - freeSpace;
        System.out.println("needed: "+needed);
    }

    private static Stack<String> names = new Stack<>();

    private static int treeSize(JSONObject node,int indent) {
        int sum=0;
        for(String key : node.keySet()) {
            if(node.get(key) instanceof JSONObject) {
                names.push(key);
                sum+=treeSize(node.getJSONObject(key),indent+2);
                names.pop();
            } else {
                sum+= (int)node.get(key);
            }
        }
        if(sum<100000)
            totalSum+=sum;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.size(); ++i) {
            sb.append(names.get(i));
            sb.append("/");
        }
        if(sum>=2677139) System.out.println(sum + "\t" + sb );

        return sum;
    }

    private static void printTree(JSONObject node,int indent) {
        for(String key : node.keySet()) {
            printIndent(indent,"- "+key);
            if(node.get(key) instanceof JSONObject) {
                System.out.println(" (dir)");
                printTree(node.getJSONObject(key),indent+1);
            } else {
                System.out.println(" (file, size="+node.get(key)+")");
            }
        }
    }

    private static void printIndent(int indent,String value) {
        System.out.print("  ".repeat(Math.max(0, indent)) +value);
    }

    private static void processLine(BufferedReader br, String line) throws IOException {
        if(line.startsWith("$")) {
            processCommand(br,line.substring(2));
        }
    }

    private static void processCommand(BufferedReader br,String command) throws IOException {
        System.out.println("command: "+command);

        if(command.startsWith("cd ..")) {
            stack.pop();
            return;
        }
        if(command.startsWith("cd /")) {
            System.out.println("jump to root");
            stack.clear();
            stack.push(root);
            return;
        }
        if(command.startsWith("cd ")) {
            // go into directory
            JSONObject now = stack.peek();
            String name = command.substring(3);
            System.out.println("go into directory "+name);
            if(!now.has(name)) {
                System.out.println("adding");
                now.put(name,new JSONObject());
            }
            System.out.println("pushing");
            stack.push(now.getJSONObject(name));
            return;
        }
        if(command.startsWith("ls")) {
            // list directory
            JSONObject now = stack.peek();

            String line;
            while(true) {
                br.mark(1000);
                line = br.readLine();
                if(line==null) return;
                if (line.startsWith("$")) {
                    br.reset();
                    break;
                }
                if (line.startsWith("dir")) {
                    String dirName = line.substring(4);
                    System.out.println("adding dir: "+dirName);
                    now.put(dirName, new JSONObject());
                } else {
                    String[] parts = line.split(" ");
                    int size = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    System.out.println("adding file: "+name);
                    now.put(name, size);
                }
            }
        }
    }
}
