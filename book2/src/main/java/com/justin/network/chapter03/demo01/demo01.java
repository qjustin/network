package com.justin.network.chapter03.demo01;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class demo01 {
    public static void main(String[] args) {
        int firstPrintableCharacter = 33;
        int numberOfPrintableCharacters = 94;
        int numberOfCharactersPerLine = 72;
        int start = firstPrintableCharacter;
        GZIPInputStream gzip;
        GZIPOutputStream
        CipherInputStream cipherInputStream;
        CipherOutputStream
        BufferedInputStream bufferedInputStream;
        OutputStream out;
        Writer
        while(true) {
            for(int i = start; i < start + numberOfCharactersPerLine; i++) {
                out.write(((i - firstPrintableCharacter) % numberOfCharactersPerLine) + firstPrintableCharacter);
            }

            out.write('\r');
            out.write('\n');
            start = ((start + 1)  - firstPrintableCharacter) % numberOfPrintableCharacters = firstPrintableCharacter;
        }
    }
}
