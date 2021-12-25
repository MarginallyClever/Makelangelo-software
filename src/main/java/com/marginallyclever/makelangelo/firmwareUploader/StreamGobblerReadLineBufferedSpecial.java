/*
 * StreamGobbler.java
 * Sources : http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 * Modif : Pierre PACAUD ( lecture char/char et divers fonction pour event via surcharge )
 */
package com.marginallyclever.makelangelo.firmwareUploader;

import java.io.*;
import java.util.Date;

/**
 * TODO revision pour les process interactif qui ne font pas de retour a la
 * ligne ... TODO ? ajout timer ou delais depuis dernier event ou info diverse
 * sur flux de sortie vers process ?
 *
 * @author p317
 */
public class StreamGobblerReadLineBufferedSpecial extends //
        //ThreadForActionEventAutoVectored
        Thread {

    InputStream is;
    String type;
    OutputStream os;
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
            // BufferedReader br = new BufferedReader(isr);

            // char cRead;
            int intRead;
            while ((intRead = isr.read()) != -1) {
                readCount++;
                // Pour le cas d'une redirection
                if (pw != null) {
                    pw.write(intRead);
                }
                //
                Date dNow = new Date();
                //
                if (intRead != '\n') {
                    stringBuilder.appendCodePoint(intRead);
                } else {
                    eventAndFlushSB(dNow);
                }
                //
                readEvent(dNow, intRead);
            }
//            String line=null;
//            while ( !haveFinish && (line = br.readLine()) != null)
//            {
//                Date dNow = new Date();
//                readLineEvent(dNow,line);
//                if (pw != null)
//                    pw.println(line);
////                if ( timeTag ) {
////                    String dateTagString = new Date().toString();
////                    System.out.println(type +"("+dateTagString+ ")>" + line);
////                }else{
//              //      System.out.println(type + ">" + line);
////                }
//                    if ( haveFinish) return;
//            }
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
     * Lecture char par char<br/>
     * A surcharger pour avoir chaque caractéres lus (byte read)
     *
     * @param d
     * @param intread
     */
    public void readEvent(Date d, int intread) {
        System.out.println("@todo StreamGobbler[" + type + "].readEvent( '" + (char) intread + "' = " + intread + " )");
    }

    /**
     * Lecture ligne par ligne<br/>
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
     * A surcharger pour avoir l'event de debut du run
     */
    public void fireUpdateRunBegin() {
    }

    /**
     * A surcharger pour avoir l'event de fin du run
     */
    public void fireUpdateRunEnd() {
    }
}
