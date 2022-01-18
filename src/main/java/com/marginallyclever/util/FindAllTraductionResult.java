/*
 * Copyright (C) 2022 Marginally Clever Robots, Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.marginallyclever.util;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.TranslatorLanguage;
import java.nio.file.Path;

/**
 * As a MatcherResult is kinf of empty if not in this .match context ... A way
 * to store some result.
 *
 * @author PPAC
 */
public class FindAllTraductionResult {

    String argsMatch;
    int posDebArgMatch = -1;
    int lineInFile = -1;
    Path pSrc;

    public FindAllTraductionResult(String argsMatch, int line, int pos, Path pSrc) {
	this.argsMatch = argsMatch;
	this.posDebArgMatch = pos;
	this.lineInFile = line;
	this.pSrc = pSrc;
    }

    //
    // Utility analyse result
    //
    /**
     * 
     *
     * @return
     */
    public boolean isArgsMatchASimpleString() {
	String tmpS = getSimpleStringFromArgs();
	if (tmpS != null) {
	    	return true;	    
	}
	return false;
    }
    
    /**
     * TODO can an expression of a simple string can be resolv ( like : "A"+"B"
     * , "\"E", ...
     * <br>
     * TODO to review for posible "\"" case. and to use it in isArgsMatchASimpleString
     * <br>
     * If the (Traduction.get Args) / "argsMatch" is a simple string, return is contente, otherwise return null;
     * @return 
     */
    public String getSimpleStringFromArgs(){
	String tmpS = argsMatch;
	if (tmpS.startsWith("\"") && tmpS.endsWith("\"")) {
	    // hopping this is a simple string // TODO otherwise
	    String justTheKey = tmpS.substring(1, tmpS.length() - 1);
	    if (justTheKey.contains("\"")) {
		//NOT a simple String // TODO (if posible) try to resolve expression like : "A"+"A" ?
		// TODO "\"" case.
		// ...
	    } else {
		return tmpS.substring(1, tmpS.length() - 1);
	    }
	}	
	return null;
    }
    
    /**
     * Only applicable/valid on a simple string args.
     * As a varaible or exression can be used as argus for Traduction.get and we only get the texte ... hard to resolv expressions.
     * This only return trye for simple String case.
     * 
     * @return 
     */
    public boolean isTraductionStartWithMissing(){
	String tmpS = getSimpleStringFromArgs();
	if ( tmpS != null ){
	     String trad = Translator.get(tmpS);
	     if ( trad.startsWith(TranslatorLanguage.PRE_APPENDED_MISSING)){ // DONE: find where "Missing:" comme from to use a varible shared in this place and not a posibly out date string value ... (if "Missing:" change )
		 return true;
	     }
	}
	return false;
    }

    //
    // For possible edition TODO ? history ? ? direct impacte vs later apply/saved ...    
    // -> a way to refresh all the TableModelConente / a way to appli modification ...
}
