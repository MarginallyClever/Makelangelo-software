/*
 * StreamGobbler.java
 * Sources (year 2012 ?) : http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 * Modif : PPAC37 ( lecture char/char et divers fonctions pour événements via surcharges de méthodes )
 */
package com.marginallyclever.makelangelo.firmwareUploader;

import java.io.*;
import java.util.Date;

/**
 * TODO revision pour les process interactif qui ne font pas de retour a la
 * ligne ... TODO ? ajout timer ou delais depuis dernier event ou info diverse
 * sur flux de sortie vers process ?
 *
 * @author PPAC37
 */
public class StreamGobblerReadLineBufferedSpecial extends Thread {

    private InputStream is;
    private String type;
    private OutputStream os;
    //
    boolean haveFinish = false;
    boolean timeTag = true;
    //
    long readCount = 0;
    int lineCount = 0;
    StringBuilder stringBuilder = null;
    //

    public String getInBuffer() {
        return stringBuilder.toString();
    }

    public StreamGobblerReadLineBufferedSpecial(InputStream is, String type) {
        this(is, type, null);
    }

    public StreamGobblerReadLineBufferedSpecial(InputStream is, String type, OutputStream redirect) {
        this.is = is;
        this.type = type;
        this.os = redirect;
        stringBuilder = new StringBuilder();
    }

    @Override
    public void run() {
        fireUpdateRunBegin();
        try {
            PrintWriter pw = null;
            if (os != null) {
                pw = new PrintWriter(os);
            }

            InputStreamReader isr = new InputStreamReader(is);
	    
            int intRead;
            while ((intRead = isr.read()) != -1) {
                readCount++;
                // Pour le cas d'une redirection
                if (pw != null) {
                    pw.write(intRead);
                }
                //
                Date dNow = new Date();
                // for line by lines events
                if (intRead != '\n') {
                    stringBuilder.appendCodePoint(intRead);
                } else {
                    eventAndFlushSB(dNow);
                }
                // for char by char events
                readEvent(dNow, intRead);
            }
	    
            if (pw != null) {
                pw.flush();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        // Pour le cas d'une ligne final sans retour a la ligne.
        if (stringBuilder.length() != 0) {
            eventAndFlushSB(new Date());
        }
        fireUpdateRunEnd();
        haveFinish = true;
        doFinish();
    }

    /**
     * Pour forcer un retour a la ligne ( dans le cas d'un flux de sortie
     * entrelasser avec des entrées ...
     */
    public void inputCarryReturn() {
        eventAndFlushSB(new Date());
    }

    /**
     *
     * @param dNow
     */
    private void eventAndFlushSB(Date dNow) {
        String lineTmp = stringBuilder.toString();
        //
        lineCount++;
        readLineEventWithCounter(dNow, lineCount, lineTmp);
        //
        readLineEvent(dNow, lineTmp);
        // on vide le buffer de la ligne
        stringBuilder.setLength(0);
    }

    /**
     * Lecture char par char.
     * A surcharger pour avoir chaque caractéres lus (byte read)
     *
     * @param d
     * @param intread
     */
    public void readEvent(Date d, int intread) {
        System.out.println("@todo StreamGobbler[" + type + "].readEvent( '" + (char) intread + "' = " + intread + " )");
    }

    /**
     * Lecture ligne par ligne.
     * A surcharger pour avoir chaque ligne lus ( cumul des byte read entre
     * chaque retour a la ligne '\n' lu )
     */
    public void readLineEvent(Date d, String s) {
        System.out.println("@todo StreamGobbler[" + type + "].readLineEvent( " + s + " )");
    }

    public void readLineEventWithCounter(Date d, int lineNum, String s) {
        System.out.println("@todo StreamGobbler[" + type + "].readLineEventWithCounter( " + s + " )");
    }

    public void doFinish() {
        haveFinish = true;
    }

    //
    //
    //
    /**
     * A surcharger pour avoir l'event de debut du run.
     */
    public void fireUpdateRunBegin() {
    }

    /**
     * A surcharger pour avoir l'event de fin du run.
     */
    public void fireUpdateRunEnd() {
    }
}
