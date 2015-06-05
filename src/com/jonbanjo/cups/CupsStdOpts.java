package com.jonbanjo.cups;

/*Copyright (C) 2013 Jon Freeman

This program is free software; you can redistribute it and/or modify it under
the terms of the GNU Lesser General Public License as published by the Free
Software Foundation; either version 3 of the License, or (at your option) any
later version.
 
This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE.
 
See the GNU Lesser General Public License for more details. You should have
received a copy of the GNU Lesser General Public License along with this
program; if not, see <http://www.gnu.org/licenses/>.
*/

public class CupsStdOpts extends CupsPpdBase{
    
    private PpdSectionList groupList;

    public CupsStdOpts(){
        createUIMap();
    }
    
    @Override
    public PpdUiList getUiList(){
        return uiList;
    }
    
    private void createUIMap(){
        uiList = new PpdUiList();
        groupList = new PpdSectionList();
        groupList.name = "Cups Standard";
        groupList.text = "Cups Standard";
        uiList.add(groupList);
        addOrientation();
        addCopies();
        addPageRanges();
        addPageSides();
        addFitToPage();
    }
    
    private void addOrientation(){
        PpdItemList sectionList = new PpdItemList();
        sectionList.name = "orientation-requested";
        sectionList.text = "Orientation";
        sectionList.commandType = CupsType.ENUM;
        sectionList.defaultValue="3";
        sectionList.savedValue = "3";
        sectionList.add(new PpdItem(sectionList, "3", "Portrait"));
        sectionList.add(new PpdItem(sectionList, "4", "Landscape"));
        sectionList.add(new PpdItem(sectionList, "5", "Reverse Portrait"));
        sectionList.add(new PpdItem(sectionList, "6", "Reverse Landscape"));
        groupList.add(sectionList);
    }
        
    private void addCopies(){
        PpdItemList sectionList = new PpdItemList();
        sectionList.name = "copies";
        sectionList.text = "Copies";
        sectionList.commandType = CupsType.INTEGER;
        sectionList.defaultValue = "1";
        sectionList.savedValue = "1";
        groupList.add(sectionList);
    }
        
    private void addPageRanges(){
        PpdItemList sectionList = new PpdItemList();
        sectionList.name = "page-ranges";
        sectionList.text = "Page Ranges";
        sectionList.commandType = CupsType.SETOFRANGEOFINTEGER;
        sectionList.defaultValue = "";
        sectionList.savedValue = "";
        groupList.add(sectionList);
    }
        
    private void addPageSides(){
        PpdItemList sectionList = new PpdItemList();
        sectionList.name = "sides";
        sectionList.text = "Page Sides";
        sectionList.commandType = CupsType.KEYWORD;
        sectionList.defaultValue = "one-sided";
        sectionList.savedValue = "one-sided";
        sectionList.add(new PpdItem(sectionList, "one-sided", "One Sided"));
        sectionList.add(new PpdItem(sectionList, "two-sided-long-edge", "Long Edge"));
        sectionList.add(new PpdItem(sectionList, "two-sided-short-edge", "Short Edge"));
        groupList.add(sectionList);
    }
        
    private void addFitToPage(){
        PpdItemList sectionList = new PpdItemList();
        sectionList.name = "fit-to-page";
        sectionList.text = "Fit To Page";
        sectionList.commandType = CupsType.BOOLEAN;
        sectionList.defaultValue = "false";
        sectionList.savedValue = "false";
        groupList.add(sectionList);
    }
        
}
