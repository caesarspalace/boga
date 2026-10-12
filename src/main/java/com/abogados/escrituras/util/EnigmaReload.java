/*
 * Copyright (C) 2026 Adrian Tabak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.abogados.escrituras.util;

import java.util.Random;
import java.util.Vector;
import java.util.Iterator;
import java.io.RandomAccessFile;
import java.io.File;


/** 
   Driver de extension de jdbc<br>
   Clases que encripta las password.
   @author Inombrable
   @version 2024-01-22
   Por mas que te creas mas de algo porque te dieron algo, siempre seras los que seras. 
*/
public class EnigmaReload
{
    class Data
    {

        public char character;
        public String hex;
        public int index;
        public String hexindex; 
        

        Data(char character, String hex, int index, String hexindex)
        {
        	this.character = character;
        	this.hex = hex;
        	this.index = index;
        	this.hexindex = hexindex;
        }
        Data(){
        }
    }

    EnigmaReload() {
    
    }
    
    public EnigmaReload(Long token)
    {
        _data = new Vector<Data>();
        srand48(token);
        cargaAbc();
        //printAbc();
        resolvData();
    }

    private int drand48(Integer stopper)
    {
        return random.nextInt(stopper);
    }
    
    private void srand48(Long seed){
    	random = new Random(seed); 
    }
    
    private void cargaAbc(){
         char _abc[]={'Z','`','Y','f','L','=','m','n','\\',';','b','W','[','H','G','\'','x','^','%','e','{','h','0','V','3','(','&','1','D','|','K','r','U','N','s','j','9','2','~','R','4','-','*','T','+','A','5','I','l','!','S','_','?','.','C','k','/','E','Q','<','o','a','X',')','#','@','7','q','8','d',' ','v','u',',','P','J','p','O','$','g','w','y','z',']','t','F',':','>','c','i','6','}','B','\"','M'};
        
        for (int i=0; i < SIZEABC; i++){
             abc[i] = (char) _abc[i]; 
        }
    }
    
   private void printAbc(){
       for (int i=0; i < SIZEABC; i++){
       }
   }
   void resolvData(){
       for (int x=0; x < SIZEABC; x++){
            Data _d = new Data();
            _d.character = abc[x];
        	_d.hex = String.format("%02x", (int)abc[x]);
            do {
               int d = drand48(127); 
               if (d < 32){
                   d += 32;
                }
                _d.index=d;
                _d.hexindex = String.format("%02x", (int)d);
              } while (buscarData(_d.index));
             _data.add(_d);
       }
    }

    private Data buscarData(char c){
       Iterator<Data> itb = _data.iterator();
       Data h=null;
       while ( itb.hasNext()) {
       	   h = (Data) itb.next();
           if (h.character == c){
                break; 
           }
       } 
       return h;
    }

    private Boolean buscarData(int index){
       Iterator<Data> itb = _data.iterator();
       Boolean flag=false;
       while ( itb.hasNext()) {
           Data  _d= (Data) itb.next();
           if (_d.index == index){
                flag=true;
                break; 
           }
       } 
       return flag;
    }

    private Data buscarData(String c){
       Iterator<Data> itb = _data.iterator();
       Data h=null;
       while ( itb.hasNext()) {
       	   h = (Data) itb.next();
           if (h.hexindex.equals(c)){
               break; 
            }
       } 
       return h;
    }


    public String getEncriptado(String cadena){
       StringBuilder str = new StringBuilder();

        for (char s : cadena.toCharArray()){
           if (s == 9){
             str.append("09");
             continue;
           }    
           Data h =  buscarData(s);
           str.append(h.hexindex);
        }
        str.append("10");
        return str.toString();
    }

    public String getDesencriptado(String s){
       StringBuilder str = new StringBuilder();
       int t=0;
        while (t < s.length() ){
           char _hexa[] = {s.charAt(t),s.charAt(t+1)};
           String hexa = new String(_hexa);
           t += 2;
           if (hexa.equals("09")){
              str.append('\t');
              continue;
           }
           if (hexa.equals("13") || hexa.equals("10")){
              str.append('\n');
              continue;
           }
               
           Data h = buscarData(hexa);
           str.append(h.character);
        }
        return str.toString();
    }

    protected void  fileEncrypt(File inputFile, File outputFile, Long token) throws Exception{
    	RandomAccessFile ifile = new RandomAccessFile(inputFile, "r");
    	RandomAccessFile ofile = new RandomAccessFile(outputFile, "rw");
    	EnigmaReload enigma = null;
   		String readLine = ifile.readLine();
    	while (readLine != null){
    	     enigma = new EnigmaReload(token);
    	     ofile.writeBytes(enigma.getEncriptado(readLine));
   	   		 readLine = ifile.readLine();
   	   	}
    }
 
    protected void fileDencrypt(File inputFile, File outputFile, Long token) throws Exception{
    	RandomAccessFile ifile = new RandomAccessFile(inputFile, "r");
    	RandomAccessFile ofile = new RandomAccessFile(outputFile, "rw");
    	EnigmaReload enigma = null;
   		String readLine = ifile.readLine();
    	while (readLine != null){
    	     enigma = new EnigmaReload(token);
    	     ofile.writeBytes(enigma.getDesencriptado(readLine));
   	   		 readLine = ifile.readLine();
   	   	}
    }
    public static void main(String... argv){ 
    	EnigmaReload _enigma;
    	if (argv.length > 2){
    		_enigma = new EnigmaReload(Long.valueOf(argv[0]));
    		if (argv[1].equals("-e")){
    			String s =  _enigma.getEncriptado(argv[2]);
    			System.out.println(s); 
    		} else if (argv[1].equals("-d")){
    			String sd =  _enigma.getDesencriptado(argv[2]);
    			System.out.println(sd); 
    		} 
    	} else{
    		System.out.println("enigma pin -e cadena");
    		System.out.println("enigma pin -d cadena");
    	}
    }

    final private int SIZEABC=95; 
    private Random random;
    private String clave;
    private Vector<Data> _data;
    private char  abc[] = new char[SIZEABC];
}
