package utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ExcelTestCaseReader {

    private static final String EXCEL_RESOURCE = "Separated_Positive_Negative_TestCases.xlsx";
    private static final DataFormatter FORMATTER = new DataFormatter();

    private ExcelTestCaseReader() {
    }

    public static List<TestCase> readSheet(String sheetName) {
        try (InputStream inputStream = ExcelTestCaseReader.class
                .getClassLoader()
                .getResourceAsStream(EXCEL_RESOURCE);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            if (inputStream == null) {
                throw new RuntimeException(EXCEL_RESOURCE + " was not found in src/test/resources");
            }

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Sheet not found: " + sheetName);
            }

            HeaderMap headers = HeaderMap.from(sheet.getRow(0));
            List<TestCase> cases = new ArrayList<>();

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowBlank(row)) {
                    continue;
                }

                String tcId = headers.value(row, "tc id", "tcid", "test case id");
                if (tcId.isBlank()) {
                    continue;
                }

                cases.add(new TestCase(
                        sheetName,
                        rowIndex + 1,
                        headers.value(row, "sr.no", "sr no", "s.no", "serial no"),
                        headers.value(row, "test scenario", "scenario"),
                        tcId,
                        headers.value(row, "test type", "type"),
                        headers.value(row, "ui/functional", "ui functional", "category"),
                        headers.value(row, "objective"),
                        headers.value(row, "pre-requisite", "pre requisite", "prerequisite"),
                        headers.value(row, "test data", "testdata"),
                        headers.value(row, "steps", "test steps"),
                        headers.value(row, "expected results", "expected result")));
            }

            return Collections.unmodifiableList(cases);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read " + EXCEL_RESOURCE, e);
        }
    }

    public static List<TestCase> readPositiveCases() {
        return readSheet("Positive");
    }

    public static List<TestCase> readNegativeCases() {
        return readSheet("Negative");
    }

    public static List<TestCase> filterByScenarioContains(String sheetName, String keyword) {
        String needle = keyword.toLowerCase(Locale.ROOT);
        return readSheet(sheetName).stream()
                .filter(tc -> tc.getTestScenario().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    public static List<TestCase> loginCases(String sheetName) {
        return filterByScenarioContains(sheetName, "cms login");
    }

    public static List<TestCase> clientCases(String sheetName) {
        return readSheet(sheetName).stream()
                .filter(tc -> {
                    String scenario = tc.getTestScenario().toLowerCase(Locale.ROOT);
                    return scenario.contains("client management")
                            || scenario.contains("license configuration")
                            || scenario.contains("company-specific login page branding");
                })
                .toList();
    }

    public static List<TestCase> rolesCases(String sheetName) {
        return readSheet(sheetName).stream()
                .filter(tc -> {
                    String scenario = tc.getTestScenario().toLowerCase(Locale.ROOT);
                    return scenario.contains("role name")
                            || scenario.contains("role type")
                            || scenario.contains("role creation")
                            || scenario.contains("associated activities");
                })
                .toList();
    }

    public static Optional<TestCase> findByTcId(String tcId) {
        return findByTcId("Positive", tcId)
                .or(() -> findByTcId("Negative", tcId));
    }

    public static Optional<TestCase> findByTcId(String sheetName, String tcId) {
        String normalized = normalize(tcId);
        for (TestCase testCase : readSheet(sheetName)) {
            if (normalize(testCase.getTcId()).equals(normalized)) {
                return Optional.of(testCase);
            }
        }
        return Optional.empty();
    }

    private static boolean isRowBlank(Row row) {
        for (int cellIndex = row.getFirstCellNum(); cellIndex < row.getLastCellNum(); cellIndex++) {
            Cell cell = row.getCell(cellIndex);
            if (cell != null && !FORMATTER.formatCellValue(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    private record HeaderMap(java.util.Map<String, Integer> columns) {

        static HeaderMap from(Row headerRow) {
            java.util.Map<String, Integer> columns = new java.util.HashMap<>();
            if (headerRow == null) {
                return new HeaderMap(columns);
            }

            for (int cellIndex = headerRow.getFirstCellNum(); cellIndex < headerRow.getLastCellNum(); cellIndex++) {
                Cell cell = headerRow.getCell(cellIndex);
                if (cell == null) {
                    continue;
                }
                String header = FORMATTER.formatCellValue(cell).trim().toLowerCase(Locale.ROOT);
                if (!header.isBlank()) {
                    columns.put(header, cellIndex);
                }
            }
            return new HeaderMap(columns);
        }

        String value(Row row, String... headerNames) {
            for (String headerName : headerNames) {
                Integer index = columns.get(headerName.toLowerCase(Locale.ROOT));
                if (index != null) {
                    Cell cell = row.getCell(index);
                    if (cell != null) {
                        return FORMATTER.formatCellValue(cell).trim();
                    }
                }
            }
            return "";
        }
    }
}
