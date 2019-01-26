package partOfDNDController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;
/**
 * Class used for managing the data inside the downloads or uploads table because
 * Every table object uses a table model object to manage the actual table data
 * This class needs inheritance to allow components (such as the progress bar status)inside of the downloads or uploads table
 *
 */
public class UpdatableTableModel extends AbstractTableModel {

        private List<RowData> rows;
        private Map<NodeFile, RowData> mapLookup;

        public UpdatableTableModel() {
            rows = new ArrayList<>(200);
            mapLookup = new HashMap<>(200);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            String name = "??";
            switch (column) {
                case 0:
                    name = "File";
                    break;
                case 1:
                    name = "Size";
                    break;
                case 2:
                    name = "Status";
                    break;
            }
            return name;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            RowData rowData = rows.get(rowIndex);
            Object value = null;
            switch (columnIndex) {
                case 0:
                    value = rowData.getFile();
                    break;
                case 1:
                    value = rowData.getLength();
                    break;
                case 2:
                    value = rowData.getStatus();
                    break;
            }
            return value;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            RowData rowData = rows.get(rowIndex);
            switch (columnIndex) {
                case 2:
                    if (aValue instanceof Float) {
                        rowData.setStatus((float) aValue);
                    }
                    break;
            }
        }
        
        public void addFile(NodeFile nfile) {
            RowData rowData = new RowData(nfile);
            mapLookup.put(nfile, rowData);
            rows.add(rowData);
            fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
        }
        
        protected void updateStatus(NodeFile nfile, int progress) {
            RowData rowData = mapLookup.get(nfile);
            if (rowData != null) {
                int row = rows.indexOf(rowData);
                float p = (float) progress / 100f;
                setValueAt(p, row, 2); //setting the new value for the status
                fireTableCellUpdated(row, 2);//triggers the rendering by the CellRenderer - ProgressCellRender
            }
        }
    	class RowData {

            private NodeFile nfile;
            private long length;
            private float status;
         
            public RowData(NodeFile nfile) {
                this.nfile = nfile;
                this.length = nfile.size;
                this.status = 0f;
            }

            public NodeFile getFile() {
                return nfile;
            }

            public long getLength() {
                return length;
            }

            public float getStatus() {
                return status;
            }

            public void setStatus(float status) {
                this.status = status;
            }
        }
    }