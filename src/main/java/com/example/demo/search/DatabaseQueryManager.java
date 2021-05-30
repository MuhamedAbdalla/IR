package com.example.demo.search;

import com.example.demo.search.Helpers.Helpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DatabaseQueryManager {

    private static final String url = "jdbc:sqlserver://DESKTOP-75A2EL9\\SQLEXPRESS:1433;databaseName=Clawer;integratedSecurity=true;";
    private static Connection connection;
    private static HashMap<String, HashSet<String>> tokenHash = null;


    public static boolean insert(SiteInfo siteInfo) {
        try {
            if(connection == null) {
                connection = DriverManager.getConnection(url);
            }
            String query = "INSERT INTO document (url, content)"
                    + "VALUES (?, ?)";
            PreparedStatement parameter = connection.prepareStatement(query);
            parameter.setString(1, siteInfo.getUrl());
            parameter.setString(2, siteInfo.getContent());
            int row = parameter.executeUpdate();
            if (row > 0) {
                return true;
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
            System.exit(0);
        }
        return false;
    }

    public static boolean insertIntoTerms(String term, int docId, int pos) {
        try {
            if(connection == null) {
                connection = DriverManager.getConnection(url);
            }

            String query = "SELECT id, freq FROM term WHERE term=? AND doc_id=?";
            PreparedStatement parameter = connection.prepareStatement(query);
            parameter.setString(1, term);
            parameter.setInt(2, docId);
            ResultSet rs = parameter.executeQuery();
            int term_id;
            if(rs.next()) {
                term_id = rs.getInt(1);
                int freq = rs.getInt(2) + 1;
                rs.close();
                query = "UPDATE term SET freq = ? where term=? and doc_id=?";
                parameter = connection.prepareStatement(query);
                parameter.setInt(1, freq);
                parameter.setString(2, term);
                parameter.setInt((int)3, docId);
                parameter.executeUpdate();
            }
            else {
                query = "INSERT INTO term (term, doc_id, freq)"
                        + "VALUES (?, ?, 1) ";
                parameter = connection.prepareStatement(query);
                parameter.setString(1, term);
                parameter.setInt(2, docId);
                parameter.executeUpdate();

                query = "SELECT id FROM term WHERE term=? AND doc_id=?";
                parameter = connection.prepareStatement(query);
                parameter.setString(1, term);
                parameter.setInt(2, docId);
                rs = parameter.executeQuery();
                rs.next();
                term_id = rs.getInt(1);
            }


            query = "INSERT INTO term_pos (term_id, pos)"
                    + "VALUES (?, ?)";
            parameter = connection.prepareStatement(query);
            parameter.setInt(1, term_id);
            parameter.setInt(2, pos);
            parameter.executeUpdate();
            return true;
        }
        catch (Exception exception) {
            exception.printStackTrace();
            System.exit(0);
        }
        return false;
    }

    public static ArrayList<SiteInfo> getAllDocs() {
        try {
            if(connection == null) {
                connection = DriverManager.getConnection(url);
            }
            String query = "SELECT id, url, content from document";
            PreparedStatement parameter = connection.prepareStatement(query);
            ResultSet rs = parameter.executeQuery();
            ArrayList<SiteInfo> docs = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt(1);
                String url = rs.getString(2);
                String content = rs.getString(3);
                docs.add(new SiteInfo(url, content, id));
            }
            return docs;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }

    public static ArrayList<SiteInfo> getDocsById(String[] ids) {
        try {
            if(connection == null) {
                connection = DriverManager.getConnection(url);
            }
            String query = "SELECT id, url, content from document where id in (" + "?,".repeat(ids.length) + ");";
            if (ids.length == 0) {
                return new ArrayList<>();
            }
            query = query.substring(0, query.length() - 3) + ");";
            PreparedStatement parameter = connection.prepareStatement(query);
            for(int i = 0; i < ids.length; i++) {
                parameter.setString(i + 1, ids[i]);
            }
            ResultSet rs = parameter.executeQuery();
            ArrayList<SiteInfo> docs = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt(1);
                String url = rs.getString(2);
                String content = rs.getString(3);
                docs.add(new SiteInfo(url, content, id));
            }
            return docs;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }

    public static HashMap<String, HashMap<String, ArrayList<Integer>>> getTokens(String[] tokens) {
        try {
            if(connection == null) {
                connection = DriverManager.getConnection(url);
            }
            String query = "SELECT doc_id, pos, term FROM term INNER JOIN term_pos ON id = term_id WHERE term IN (" +
                    "?,".repeat(tokens.length) + ");";
            if (tokens.length == 0) {
                return new HashMap<>();
            }
            query = query.substring(0, query.length() - 3) + ");";
            PreparedStatement parameter = connection.prepareStatement(query);
            for(var i = 0; i < tokens.length; i++) {
                parameter.setString(i + 1, tokens[i]);
            }
            ResultSet rs = parameter.executeQuery();
            HashMap<String, HashMap<String, ArrayList<Integer>>> docs = new HashMap<>();
            while (rs.next()) {
                String doc_id = rs.getString(1);
                int pos = rs.getInt(2);
                String term = rs.getString(3);
                docs.computeIfAbsent(doc_id, k -> new HashMap<>()).computeIfAbsent(term, k -> new ArrayList<>()).add(pos);
            }
            return docs;

        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String[] getPureTokensStartsWith(String pattern) {
        try {
            if(connection == null) {
                connection = DriverManager.getConnection(url);
            }
            String query = "SELECT term FROM term_no_stemming  WHERE term LIKE ?;";
            PreparedStatement parameter = connection.prepareStatement(query);
            parameter.setString(1, pattern + "%");
            ResultSet rs = parameter.executeQuery();
            ArrayList<String> tokens = new ArrayList<>();
            while (rs.next()) {
                String token = rs.getString(1);
                tokens.add(token);
            }
            return tokens.toArray(String[]::new);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String[] getPureTokensEndsWith(String pattern) {
        try {
            if(connection == null) {
                connection = DriverManager.getConnection(url);
            }
            String query = "SELECT term FROM term_no_stemming  WHERE term LIKE ?;";
            PreparedStatement parameter = connection.prepareStatement(query);
            parameter.setString(1, "%" + pattern);
            ResultSet rs = parameter.executeQuery();
            ArrayList<String> tokens = new ArrayList<>();
            while (rs.next()) {
                String token = rs.getString(1);
                tokens.add(token);
            }
            return tokens.toArray(String[]::new);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String[] getPureTokensContains(String pattern) {
        try {
            if(connection == null) {
                connection = DriverManager.getConnection(url);
            }
            String query = "SELECT term FROM term_no_stemming  WHERE term LIKE ?;";
            PreparedStatement parameter = connection.prepareStatement(query);
            parameter.setString(1, "%" + pattern + "%");
            ResultSet rs = parameter.executeQuery();
            ArrayList<String> tokens = new ArrayList<>();
            while (rs.next()) {
                String token = rs.getString(1);
                tokens.add(token);
            }
            return tokens.toArray(String[]::new);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, HashSet<String>> getAllTermsWithNoStemming() {
        try {
            if(connection == null) {
                connection = DriverManager.getConnection(url);
            }
            if(tokenHash != null) {
                return tokenHash;
            }
            String query = "SELECT term FROM term_no_stemming;";
            PreparedStatement parameter = connection.prepareStatement(query);
            ResultSet rs = parameter.executeQuery();
            tokenHash = new HashMap<>();
            while (rs.next()) {
                String token = rs.getString(1);
                var hash = Helpers.applySoundex(token);
                tokenHash.computeIfAbsent(hash, s -> new HashSet<>()).add(token);
            }
            return tokenHash;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void closeConnection() {
        try {
            if(connection != null) {
                connection.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
