/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.vbs;

/**
 * This class contains an enumeration of various constants that VBS2 uses for
 * various parameters.
 *
 * @author pkv
 */
public class Vbs2Constants {

    /**
     * Enumerates a list of basic types of object in VBS2 that may need to
     * be handled differently in different situations.
     */
    public enum Type {
        UNIT,
        AERIAL,
        VEHICLE;
    }

    /**
     * Contains a list of useful units in VBS2 and their necessary
     * initialization information.
     */
    public enum Object {

        REAPER("vbs2_GB_RAF_Reaper2", Type.AERIAL),
        MERLIN("VBS2_GB_RAF_Merlin_HC3", Type.AERIAL),
        LONGBOW("VBS2_GB_WAH64D_LONGBOW", Type.AERIAL),
        CAMEL("vbs2_animal_camel_lightbrown_none", Type.UNIT),
        HMMWV("HMMWV", Type.VEHICLE),
        USMC_RAC("VBS2_US_MC_RAC", Type.VEHICLE),
        DOUBLEEAGLE_ROV("VBS2_AU_RAN_DoubleEagle_ROV", Type.VEHICLE),
        RED_CAR("SkodaRed", Type.VEHICLE),
        ZODIAC("Zodiac", Type.VEHICLE),
        CREW("SoldierWCrew", Type.UNIT);

        public final String ident;
        public final Type type;

        Object(String id, Type t) {
            ident = id;
            type = t;
        }
    }
}
