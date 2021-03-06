package com.nearhop.nearhop.parser;


import com.nearhop.nearhop.db.Database;

public interface Parser {

    /**
     * Parses a line of data from a foreign source.
     *
     * @param line
     * @return
     */
    String[] parseLine(String line);

    /**
     * Saves a parsed line of data to the database.
     *
     * @param db
     * @param data
     * @return
     */
    long saveLine(Database db, String[] data);

}
