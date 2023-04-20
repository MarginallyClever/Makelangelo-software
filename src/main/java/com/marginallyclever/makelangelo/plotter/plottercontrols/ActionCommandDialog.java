package com.marginallyclever.makelangelo.plotter.plottercontrols;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * {@link ActionCommandDialog} is a dialog that allows the user to enter a question and a list of choice responses.
 * It was written to be used by Marlin Action Commands.  See {@link MarlinPanel} for more information.
 */
public class ActionCommandDialog {
    private String promptMessage = "";
    private final List<String> options = new ArrayList<>();
    private JDialog dialog;
    private int result = -1;

    public ActionCommandDialog() {}

    public void run(JComponent parentComponent, String title, Consumer<Integer> consumer) {
        String[] choices = options.toArray(new String[0]);

        JOptionPane pane = new JOptionPane(promptMessage, JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, choices, choices[0]);
        dialog = pane.createDialog(parentComponent, title);
        dialog.setVisible(true);
        result = saveResult(pane, choices);
        consumer.accept(result);
        dialog.dispose();
        dialog=null;
    }

    public int getResult() {
        return result;
    }

    public int saveResult(JOptionPane pane,String[] choices) {
        Object selectedValue = pane.getValue();
        if(selectedValue == null)
            return JOptionPane.CLOSED_OPTION;
        //If there is not an array of option buttons:
        if(choices == null) {
            if(selectedValue instanceof Integer)
                return (Integer) selectedValue;
            return JOptionPane.CLOSED_OPTION;
        }
        //If there is an array of option buttons:
        for(int counter = 0, maxCounter = choices.length;
            counter < maxCounter; counter++) {
            if(choices[counter].equals(selectedValue))
                return counter;
        }
        return JOptionPane.CLOSED_OPTION;
    }

    public void addOption(String s) {
        options.add(s);
    }

    public void setPromptMessage(String s) {
        promptMessage = s;
    }

    public void clearPrompts() {
        options.clear();
    }

    public void close() {
        if(dialog!=null) {
            dialog.setVisible(false);
            dialog=null;
        }
    }

    public static void main(String[] args) {
        ActionCommandDialog d = new ActionCommandDialog();
        d.setPromptMessage("What is your favorite color?");
        d.addOption("Red");
        d.addOption("Green");
        d.addOption("Blue");
        d.run(null, "Color", (Integer i) -> {
            System.out.println("You chose " + i);
        });
    }

    public boolean isOpen() {
        return dialog!=null;
    }
}
