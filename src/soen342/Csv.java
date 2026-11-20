package soen342;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

final class Csv {
    record Table(List<String> header, List<List<String>> rows) {}

    private Csv() {}

    static Table read(Path file) throws IOException {
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String firstLine = reader.readLine();
            if (firstLine == null) {
                throw new IllegalArgumentException("The selected CSV file is empty. Choose a CSV with a header row.");
            }
            if (firstLine.startsWith("\uFEFF")) {
                firstLine = firstLine.substring(1);
            }
            var header = parse(firstLine, 1);
            var rows = new ArrayList<List<String>>();
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                var fields = parse(line, lineNumber);
                if (fields.size() != header.size()) {
                    throw new IllegalArgumentException("CSV line " + lineNumber + " has " + fields.size()
                            + " fields; expected " + header.size() + " to match the header.");
                }
                rows.add(fields);
            }
            return new Table(header, rows);
        }
    }

    static List<List<String>> read(Path file, List<String> expectedHeader) throws IOException {
        Table table = read(file);
        if (!table.header().equals(expectedHeader)) {
            throw new IllegalArgumentException("CSV columns do not match the required header: "
                    + String.join(",", expectedHeader));
        }
        return table.rows();
    }

    static void write(Path file, List<String> header, List<List<String>> rows) throws IOException {
        Path target = file.toAbsolutePath().normalize();
        if (Files.isSymbolicLink(target)) {
            target = target.toRealPath();
        }
        if (target.getParent() == null || Files.isDirectory(target)) {
            throw new IOException("Select an output CSV file, not a directory");
        }
        Path export = Files.createTempFile(target.getParent(), ".soen342-export-", ".csv");
        try {
            try (var writer = Files.newBufferedWriter(export, StandardCharsets.UTF_8)) {
                writer.write(format(header));
                writer.newLine();
                for (var row : rows) {
                    writer.write(format(row));
                    writer.newLine();
                }
            }
            try {
                Files.move(export, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(export, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(export);
        }
    }

    static <T> void putRecord(java.util.Map<Integer, T> records, int id, T record) {
        T existing = records.putIfAbsent(id, record);
        if (existing != null && !existing.equals(record)) {
            throw new IllegalArgumentException("CSV ID " + id
                    + " is used for different records. Each ID must identify only one record.");
        }
    }

    static int id(String field) {
        int id;
        try {
            id = Integer.parseInt(field);
        } catch (NumberFormatException exception) {
            throw new NumberFormatException("Enter a positive whole number for an ID.");
        }
        if (id < 1) {
            throw new IllegalArgumentException("Enter a positive whole number for an ID.");
        }
        return id;
    }

    static String required(String field, String label) {
        if (field.isBlank()) {
            throw new IllegalArgumentException("Enter a value for " + label + ".");
        }
        return field;
    }

    private static List<String> parse(String line, int lineNumber) {
        var fields = new ArrayList<String>();
        var field = new StringBuilder();
        boolean quoted = false;
        boolean closedQuote = false;
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (quoted) {
                if (character != '"') {
                    field.append(character);
                } else if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    quoted = false;
                    closedQuote = true;
                }
            } else if (character == ',') {
                fields.add(field.toString().strip());
                field.setLength(0);
                closedQuote = false;
            } else if (character == '"' && field.isEmpty() && !closedQuote) {
                quoted = true;
            } else if (character == '"' || closedQuote) {
                throw new IllegalArgumentException("CSV line " + lineNumber
                        + " has invalid quotation marks. Check the field's opening and closing quotes.");
            } else {
                field.append(character);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("CSV line " + lineNumber
                    + " has an opening quotation mark without a matching closing mark.");
        }
        fields.add(field.toString().strip());
        return fields;
    }

    private static String format(List<String> fields) {
        var escaped = new ArrayList<String>(fields.size());
        for (String field : fields) {
            if (field.indexOf(',') != -1 || field.indexOf('"') != -1) {
                escaped.add("\"" + field.replace("\"", "\"\"") + "\"");
            } else {
                escaped.add(field);
            }
        }
        return String.join(",", escaped);
    }
}
