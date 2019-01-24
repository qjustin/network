//package com.justin.network.chapter03.demo01;
//
//public class demo01 {
//    public static void main(String[] args) {
//        int firstPrintableCharacter = 33;
//        int numberOfPrintableCharacters = 94;
//        int numberOfCharactersPerLine = 72;
//        int start = firstPrintableCharacter;
//
//
//        while(true) {
//            for(int i = start; i < start + numberOfCharactersPerLine; i++) {
//                out.write(((i - firstPrintableCharacter) % numberOfCharactersPerLine) + firstPrintableCharacter);
//            }
//
//            out.write('\r');
//            out.write('\n');
//            start = ((start + 1)  - firstPrintableCharacter) % numberOfPrintableCharacters = firstPrintableCharacter;
//        }
//    }
//}
