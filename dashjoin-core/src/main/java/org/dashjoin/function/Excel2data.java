package org.dashjoin.function;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Loads an excel sheet into a json structure (map of sheetname to table). we interpret the first
 * row as the table field names
 */
public abstract class Excel2data extends AbstractVarArgFunction<Object> {

  protected FormulaEvaluator evaluator;

  @Override
  public String getType() {
    return "read";
  }
  
  protected Object parse(InputStream in) throws Exception {
    Workbook wb = WorkbookFactory.create(in);
    evaluator = wb.getCreationHelper().createFormulaEvaluator();
    Map<String, List<Map<String, Object>>> res = new LinkedHashMap<>();
    for (Sheet sheet : wb) {
      Iterator<Row> records = sheet.iterator();
      List<Map<String, Object>> table = new ArrayList<>();
      res.put(sheet.getSheetName(), table);

      Map<Integer, String> cols = new LinkedHashMap<>();
      for (Cell cell : records.next()) {
        Object val = getCellValue(cell);
        if (val != null)
          cols.put(cell.getColumnIndex(), "" + val);
      }

      while (records.hasNext()) {
        Map<String, Object> row = new LinkedHashMap<>();
        table.add(row);
        for (Cell cell : records.next())
          if (cols.get(cell.getColumnIndex()) != null)
            row.put(cols.get(cell.getColumnIndex()), getCellValue(cell));
      }
    }
    return res;
  }

  protected Object getCellValue(Cell cell) {
    return o(evaluator.evaluate(cell));
  }

  protected Object o(CellValue cellValue) {
    if (cellValue == null)
      return null;
    else if (cellValue.getCellType() == CellType.BOOLEAN)
      return cellValue.getBooleanValue();
    else if (cellValue.getCellType() == CellType.NUMERIC)
      return cellValue.getNumberValue();
    else if (cellValue.getCellType() == CellType.STRING)
      return cellValue.getStringValue();
    else if (cellValue.getCellType() == CellType.ERROR)
      return null;
    else if (cellValue.getCellType() == CellType.BLANK)
      return null;
    else
      throw new RuntimeException("Illegal cell type");
  }

  @Override
  public List<Class> getArgumentClassList() {
    return List.of(String.class, Object.class);
  }
}
