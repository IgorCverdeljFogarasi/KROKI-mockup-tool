/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kroki.profil.utils.visitor;

import kroki.profil.subsystem.BussinesSubsystem;
import kroki.uml_core_basic.UmlClass;
import kroki.uml_core_basic.UmlPackage;
import kroki.uml_core_basic.UmlProperty;

/**
 *
 * @author Vladan Marsenić (vladan.marsenic@gmail.com)
 */
public class AllPosibleZoomPanels extends Visitor {

    public void visit(Object object) {
        UmlProperty property = null;
        if (object instanceof UmlProperty) {
            property = (UmlProperty) object;
        } else {
            System.err.println("AllPossibleZoomPanels.java - 23: Object cannot be cast to UmlProperty");
            return;
        }
        UmlClass umlClass = property.umlClass();
        if (umlClass == null) {
            System.err.println("AllPossibleZoomPanels.java - 28: UmlProperty's class is null");
            return;
        }
        UmlPackage umlPackage = umlClass.umlPackage();
        if (umlPackage == null) {
            System.err.println("AllPossibleZoomPanels.java - 33: UmlClass's package is null");
            return;
        }
        while (true) {
            if (umlPackage.nestingPackage() == null) {
                break;
            } else {
                umlPackage = umlPackage.nestingPackage();
            }
        }
        ((BussinesSubsystem) umlPackage).accept(this);
    }
}
