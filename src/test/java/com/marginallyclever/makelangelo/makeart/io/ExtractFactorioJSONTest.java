package com.marginallyclever.makelangelo.makeart.io;

import javax.swing.*;
import java.awt.*;
import java.util.zip.DataFormatException;

public class ExtractFactorioJSONTest {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("Extract Factorio JSON");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        JPanel panel = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        JTextArea input = new JTextArea();
        JTextArea output = new JTextArea();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.gridx=0;
        gbc.gridy=0;
        input.setLineWrap(true);
        output.setLineWrap(true);
        panel.add(new JScrollPane(input), gbc);
        gbc.gridy=1;
        panel.add(new JScrollPane(output), gbc);
        frame.setContentPane(panel);

        input.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                output.setText(processInputInternal(input.getText()));
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                output.setText(processInputInternal(input.getText()));
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                output.setText(processInputInternal(input.getText()));
            }
        });

        frame.setVisible(true);
    }

    private static String processInputInternal(String text) {
        try {
            return prettyJSON(ExtractFactorioJSON.processInput(text));
        }
        catch (DataFormatException ex) {
            return "Error decompressing data: " + ex.getMessage();
        }
        catch( Exception ex) {
            return "Error parsing json: " + ex.getMessage();
        }
    }

    /**
     * format json for readability
     * @param original the original json string
     * @return the formatted json string
     */
    private static String prettyJSON(String original) {
        int indent = 0;
        StringBuilder pretty = new StringBuilder();
        int size= original.length();
        for(int i = 0; i < size; i++) {
            char c = original.charAt(i);
            pretty.append(c);
            if (c == '{' || c == '[') {
                indent++;
                pretty.append('\n');
                addIndent(pretty,indent);
            } else if (c == '}' || c == ']') {
                indent--;
                if(i<size-1 && original.charAt(i+1) != ',') {
                    pretty.append('\n');
                    addIndent(pretty,indent);
                }
            } else if(c==',') {
                pretty.append('\n');
                addIndent(pretty,indent);
            }
        }
        return pretty.toString();
    }

    private static void addIndent(StringBuilder sb, int indent) {
        sb.append("  ".repeat(Math.max(0, indent)));
    }
}
