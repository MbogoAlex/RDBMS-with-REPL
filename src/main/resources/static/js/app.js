// API Base URL
const API_URL = '/api';

// Terminal history
let commandHistory = [];
let historyIndex = -1;

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    checkHealth();
    refreshTables();
    setupTerminal();
    setInterval(refreshTables, 10000); // Auto-refresh tables every 10s
    
    // Add first column field by default
    addColumnField();
});

// Check database health
async function checkHealth() {
    try {
        const response = await fetch(`${API_URL}/health`);
        const data = await response.json();
        document.getElementById('status-text').textContent = data.status === 'UP' ? 'Connected' : 'Disconnected';
    } catch (error) {
        document.getElementById('status-text').textContent = 'Error';
        document.getElementById('status-indicator').style.background = '#dc2626';
    }
}

// Refresh tables list
async function refreshTables() {
    try {
        const response = await fetch(`${API_URL}/tables`);
        const tables = await response.json();
        displayTablesList(tables);
        updateTableSelects(tables);
    } catch (error) {
        console.error('Error fetching tables:', error);
    }
}

// Display tables in sidebar
function displayTablesList(tables) {
    const container = document.getElementById('tables-list');
    if (tables.length === 0) {
        container.innerHTML = '<p class="loading">No tables yet</p>';
        return;
    }
    
    container.innerHTML = tables.map(table => `
        <div class="table-item" onclick="selectTable('${table}')">
            📋 ${table}
        </div>
    `).join('');
}

// Update table dropdowns
function updateTableSelects(tables) {
    const selects = ['selected-table', 'query-table'];
    selects.forEach(id => {
        const select = document.getElementById(id);
        if (select) {
            select.innerHTML = '<option value="">-- Select a table --</option>' +
                tables.map(table => `<option value="${table}">${table}</option>`).join('');
        }
    });
}

// Select table
function selectTable(tableName) {
    document.getElementById('selected-table').value = tableName;
    loadTableData();
}

// Tab switching
function showTab(tabName) {
    // Remove active class from all tabs and buttons
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    
    // Add active class to selected tab
    document.getElementById(`${tabName}-tab`).classList.add('active');
    event.target.classList.add('active');
}

// ==================== TERMINAL FUNCTIONS ====================

function setupTerminal() {
    const input = document.getElementById('terminal-input');
    
    input.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            const command = input.value.trim();
            if (command) {
                executeCommand(command);
                commandHistory.push(command);
                historyIndex = commandHistory.length;
                input.value = '';
            }
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            if (historyIndex > 0) {
                historyIndex--;
                input.value = commandHistory[historyIndex];
            }
        } else if (e.key === 'ArrowDown') {
            e.preventDefault();
            if (historyIndex < commandHistory.length - 1) {
                historyIndex++;
                input.value = commandHistory[historyIndex];
            } else {
                historyIndex = commandHistory.length;
                input.value = '';
            }
        }
    });
}

async function executeCommand(command) {
    const output = document.getElementById('terminal-output');
    
    // Add command to terminal
    addTerminalLine(`<span class="terminal-command">duka> ${escapeHtml(command)}</span>`);
    
    // Handle special commands
    if (command.toLowerCase() === 'help') {
        showHelp();
        return;
    }
    
    if (command.toLowerCase() === 'clear') {
        clearTerminal();
        return;
    }
    
    // Execute SQL command
    try {
        const response = await fetch(`${API_URL}/execute`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sql: command })
        });
        
        const result = await response.json();
        displayQueryResult(result);
        
        // Refresh tables after CREATE/DROP
        if (command.toUpperCase().includes('CREATE TABLE') || command.toUpperCase().includes('DROP TABLE')) {
            setTimeout(refreshTables, 500);
        }
    } catch (error) {
        addTerminalLine(`<span class="terminal-error">✗ Error: ${error.message}</span>`);
    }
}

function displayQueryResult(result) {
    if (!result.success) {
        addTerminalLine(`<span class="terminal-error">✗ ${escapeHtml(result.message)}</span>`);
        return;
    }
    
    // If there are rows, display as table
    if (result.rows && result.rows.length > 0) {
        const table = formatResultTable(result.columnNames, result.rows);
        addTerminalLine(`<pre class="terminal-table">${table}</pre>`);
        addTerminalLine(`<span class="terminal-success">${result.rowCount} row(s) returned (${result.executionTimeMs} ms)</span>`);
    } else {
        addTerminalLine(`<span class="terminal-success">✓ ${escapeHtml(result.message)}</span>`);
        if (result.executionTimeMs) {
            addTerminalLine(`<span class="terminal-output-text">(${result.executionTimeMs} ms)</span>`);
        }
    }
}

function formatResultTable(columns, rows) {
    if (!columns || !rows) return '';
    
    // Calculate column widths
    const widths = columns.map((col, i) => {
        const maxDataWidth = Math.max(...rows.map(row => String(row[col] || '').length));
        return Math.max(col.length, maxDataWidth) + 2;
    });
    
    // Build table
    let table = '';
    
    // Top border
    table += '+' + widths.map(w => '-'.repeat(w)).join('+') + '+\n';
    
    // Header
    table += '|' + columns.map((col, i) => ` ${col.padEnd(widths[i] - 1)}`).join('|') + '|\n';
    
    // Middle border
    table += '+' + widths.map(w => '-'.repeat(w)).join('+') + '+\n';
    
    // Rows
    rows.forEach(row => {
        table += '|' + columns.map((col, i) => {
            const value = String(row[col] !== null && row[col] !== undefined ? row[col] : '');
            return ` ${value.padEnd(widths[i] - 1)}`;
        }).join('|') + '|\n';
    });
    
    // Bottom border
    table += '+' + widths.map(w => '-'.repeat(w)).join('+') + '+\n';
    
    return table;
}

function addTerminalLine(html) {
    const output = document.getElementById('terminal-output');
    const line = document.createElement('div');
    line.className = 'terminal-line';
    line.innerHTML = html;
    output.appendChild(line);
    output.scrollTop = output.scrollHeight;
}

function clearTerminal() {
    const output = document.getElementById('terminal-output');
    output.innerHTML = '<div class="terminal-welcome">Terminal cleared. Type SQL commands or \'help\' for examples.</div>';
}

function showHelp() {
    const help = `
<span class="terminal-success">Available Commands:</span>

<span class="terminal-output-text">CREATE TABLE products (id INT PRIMARY KEY, name VARCHAR(100), price INT)</span>
<span class="terminal-output-text">INSERT INTO products VALUES (1, 'Laptop', 75000)</span>
<span class="terminal-output-text">SELECT * FROM products</span>
<span class="terminal-output-text">SELECT * FROM products WHERE price > 10000</span>
<span class="terminal-output-text">UPDATE products SET price = 80000 WHERE id = 1</span>
<span class="terminal-output-text">DELETE FROM products WHERE id = 1</span>
<span class="terminal-output-text">DROP TABLE products</span>

<span class="terminal-success">Special Commands:</span>
<span class="terminal-output-text">help  - Show this help</span>
<span class="terminal-output-text">clear - Clear terminal</span>
    `;
    addTerminalLine(help);
}

function showHistory() {
    if (commandHistory.length === 0) {
        addTerminalLine('<span class="terminal-output-text">No command history yet</span>');
        return;
    }
    
    addTerminalLine('<span class="terminal-success">Command History:</span>');
    commandHistory.forEach((cmd, i) => {
        addTerminalLine(`<span class="terminal-output-text">${i + 1}. ${escapeHtml(cmd)}</span>`);
    });
}

// ==================== GUI MODE FUNCTIONS ====================

let columnCounter = 0;

function addColumnField() {
    const container = document.getElementById('columns-container');
    
    // Remove empty message if exists
    const emptyMsg = container.querySelector('.empty-columns');
    if (emptyMsg) {
        emptyMsg.remove();
    }
    
    const columnId = `col-${columnCounter++}`;
    
    const columnRow = document.createElement('div');
    columnRow.className = 'column-row';
    columnRow.id = columnId;
    columnRow.innerHTML = `
        <input type="text" placeholder="Column name" class="col-name" data-col="${columnId}">
        <select class="col-type" data-col="${columnId}">
            <option value="">-- Select type --</option>
            <option value="INT">INT</option>
            <option value="LONG">LONG</option>
            <option value="VARCHAR">VARCHAR</option>
            <option value="BOOLEAN">BOOLEAN</option>
            <option value="DATE">DATE</option>
            <option value="DATETIME">DATETIME</option>
            <option value="TIMESTAMP">TIMESTAMP</option>
        </select>
        <input type="number" placeholder="Size" class="col-size" data-col="${columnId}" min="1" max="1000" style="display: none;">
        <label><input type="checkbox" class="col-primary" data-col="${columnId}"> PK</label>
        <label><input type="checkbox" class="col-unique" data-col="${columnId}"> Unique</label>
        <label><input type="checkbox" class="col-notnull" data-col="${columnId}"> Not Null</label>
        <button onclick="removeColumnField('${columnId}')" class="btn-remove">✕</button>
    `;
    
    container.appendChild(columnRow);
    
    // Show size input for VARCHAR
    const typeSelect = columnRow.querySelector('.col-type');
    const sizeInput = columnRow.querySelector('.col-size');
    
    typeSelect.addEventListener('change', () => {
        if (typeSelect.value === 'VARCHAR') {
            sizeInput.style.display = 'block';
            sizeInput.value = '100';
        } else {
            sizeInput.style.display = 'none';
            sizeInput.value = '';
        }
    });
}

function removeColumnField(columnId) {
    const columnRow = document.getElementById(columnId);
    if (columnRow) {
        columnRow.remove();
    }
    
    // Show empty message if no columns
    const container = document.getElementById('columns-container');
    if (container.children.length === 0) {
        container.innerHTML = '<div class="empty-columns">No columns added yet. Click "Add Column" to start.</div>';
    }
}

async function createTableFromGUI() {
    const tableName = document.getElementById('new-table-name').value.trim();
    
    if (!tableName) {
        alert('Please enter a table name');
        return;
    }
    
    const container = document.getElementById('columns-container');
    const columnRows = container.querySelectorAll('.column-row');
    
    if (columnRows.length === 0) {
        alert('Please add at least one column');
        return;
    }
    
    // Build column definitions
    const columns = [];
    let hasError = false;
    
    columnRows.forEach(row => {
        const name = row.querySelector('.col-name').value.trim();
        const type = row.querySelector('.col-type').value;
        const size = row.querySelector('.col-size').value;
        const isPrimary = row.querySelector('.col-primary').checked;
        const isUnique = row.querySelector('.col-unique').checked;
        const isNotNull = row.querySelector('.col-notnull').checked;
        
        if (!name || !type) {
            hasError = true;
            return;
        }
        
        let columnDef = `${name} `;
        
        if (type === 'VARCHAR') {
            const varcharSize = size || '100';
            columnDef += `VARCHAR(${varcharSize})`;
        } else {
            columnDef += type;
        }
        
        if (isPrimary) columnDef += ' PRIMARY KEY';
        if (isUnique && !isPrimary) columnDef += ' UNIQUE';
        if (isNotNull && !isPrimary) columnDef += ' NOT NULL';
        
        columns.push(columnDef);
    });
    
    if (hasError) {
        alert('Please fill in all column names and types');
        return;
    }
    
    if (columns.length === 0) {
        alert('Please add at least one valid column');
        return;
    }
    
    const sql = `CREATE TABLE ${tableName} (${columns.join(', ')})`;
    
    try {
        const response = await fetch(`${API_URL}/execute`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sql })
        });
        
        const result = await response.json();
        if (result.success) {
            alert('Table created successfully!');
            
            // Clear form
            document.getElementById('new-table-name').value = '';
            document.getElementById('columns-container').innerHTML = '';
            addColumnField();
            
            refreshTables();
        } else {
            alert('Error: ' + result.message);
        }
    } catch (error) {
        alert('Error creating table: ' + error.message);
    }
}

async function createTable() {
    const tableName = document.getElementById('new-table-name').value.trim();
    const columnsText = document.getElementById('new-table-columns').value.trim();
    
    if (!tableName || !columnsText) {
        alert('Please provide table name and columns');
        return;
    }
    
    const sql = `CREATE TABLE ${tableName} (${columnsText})`;
    
    try {
        const response = await fetch(`${API_URL}/execute`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sql })
        });
        
        const result = await response.json();
        if (result.success) {
            alert('Table created successfully!');
            document.getElementById('new-table-name').value = '';
            document.getElementById('new-table-columns').value = '';
            refreshTables();
        } else {
            alert('Error: ' + result.message);
        }
    } catch (error) {
        alert('Error creating table: ' + error.message);
    }
}

async function loadTableData() {
    const tableName = document.getElementById('selected-table').value;
    if (!tableName) return;
    
    try {
        // Get table schema
        const schemaResponse = await fetch(`${API_URL}/tables/${tableName}/schema`);
        const schema = await schemaResponse.json();
        displayTableSchema(schema);
        
        // Get table data
        const dataResponse = await fetch(`${API_URL}/execute`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sql: `SELECT * FROM ${tableName}` })
        });
        
        const data = await dataResponse.json();
        displayTableData(data, schema);
        
    } catch (error) {
        console.error('Error loading table data:', error);
    }
}

function displayTableSchema(schema) {
    const container = document.getElementById('table-info');
    container.innerHTML = `
        <h4>Table: ${schema.tableName}</h4>
        <table class="data-table">
            <thead>
                <tr>
                    <th>Column</th>
                    <th>Type</th>
                    <th>Primary</th>
                    <th>Unique</th>
                    <th>Nullable</th>
                </tr>
            </thead>
            <tbody>
                ${schema.columns.map(col => `
                    <tr>
                        <td>${col.name}</td>
                        <td>${col.dataType}</td>
                        <td>${col.primaryKey ? '✓' : ''}</td>
                        <td>${col.unique ? '✓' : ''}</td>
                        <td>${col.nullable ? '✓' : ''}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
        <p><strong>Row Count:</strong> ${schema.rowCount}</p>
    `;
    
    // Show insert form
    displayInsertForm(schema);
}

function displayInsertForm(schema) {
    const container = document.getElementById('insert-fields');
    container.innerHTML = schema.columns.map(col => `
        <div class="form-group">
            <label>${col.name} (${col.dataType}):</label>
            <input type="text" id="field-${col.name}" class="input" placeholder="Enter ${col.name}">
        </div>
    `).join('');
    
    document.getElementById('data-form').style.display = 'block';
}

async function insertData() {
    const tableName = document.getElementById('selected-table').value;
    if (!tableName) return;
    
    try {
        const schemaResponse = await fetch(`${API_URL}/tables/${tableName}/schema`);
        const schema = await schemaResponse.json();
        
        const values = schema.columns.map(col => {
            const value = document.getElementById(`field-${col.name}`).value;
            if (col.dataType.includes('VARCHAR')) {
                return `'${value}'`;
            }
            return value || 'NULL';
        }).join(', ');
        
        const sql = `INSERT INTO ${tableName} VALUES (${values})`;
        
        const response = await fetch(`${API_URL}/execute`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sql })
        });
        
        const result = await response.json();
        if (result.success) {
            alert('Data inserted successfully!');
            schema.columns.forEach(col => {
                document.getElementById(`field-${col.name}`).value = '';
            });
            loadTableData();
        } else {
            alert('Error: ' + result.message);
        }
    } catch (error) {
        alert('Error inserting data: ' + error.message);
    }
}

function displayTableData(data, schema) {
    const container = document.getElementById('table-data');
    
    if (!data.success || !data.rows || data.rows.length === 0) {
        container.innerHTML = '<p class="loading">No data in table</p>';
        return;
    }
    
    container.innerHTML = `
        <table class="data-table">
            <thead>
                <tr>${data.columnNames.map(col => `<th>${col}</th>`).join('')}</tr>
            </thead>
            <tbody>
                ${data.rows.map(row => `
                    <tr>
                        ${data.columnNames.map(col => `<td>${row[col] !== null ? row[col] : 'NULL'}</td>`).join('')}
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

async function dropSelectedTable() {
    const tableName = document.getElementById('selected-table').value;
    if (!tableName) {
        alert('Please select a table');
        return;
    }
    
    if (!confirm(`Are you sure you want to drop table "${tableName}"? This cannot be undone!`)) {
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/execute`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sql: `DROP TABLE ${tableName}` })
        });
        
        const result = await response.json();
        if (result.success) {
            alert('Table dropped successfully!');
            document.getElementById('selected-table').value = '';
            document.getElementById('table-info').innerHTML = '';
            document.getElementById('table-data').innerHTML = '';
            document.getElementById('data-form').style.display = 'none';
            refreshTables();
        } else {
            alert('Error: ' + result.message);
        }
    } catch (error) {
        alert('Error dropping table: ' + error.message);
    }
}

async function truncateSelectedTable() {
    const tableName = document.getElementById('selected-table').value;
    if (!tableName) {
        alert('Please select a table');
        return;
    }
    
    if (!confirm(`Delete all data from table "${tableName}"?`)) {
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/execute`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sql: `DELETE FROM ${tableName}` })
        });
        
        const result = await response.json();
        if (result.success) {
            alert('All data deleted!');
            loadTableData();
        } else {
            alert('Error: ' + result.message);
        }
    } catch (error) {
        alert('Error deleting data: ' + error.message);
    }
}

// ==================== QUERY BUILDER ====================

async function buildAndExecuteQuery() {
    const table = document.getElementById('query-table').value;
    const columns = document.getElementById('query-columns').value.trim() || '*';
    const where = document.getElementById('query-where').value.trim();
    
    if (!table) {
        alert('Please select a table');
        return;
    }
    
    let sql = `SELECT ${columns} FROM ${table}`;
    if (where) {
        sql += ` WHERE ${where}`;
    }
    
    try {
        const response = await fetch(`${API_URL}/execute`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sql })
        });
        
        const result = await response.json();
        displayQueryBuilderResult(result);
    } catch (error) {
        document.getElementById('query-result').innerHTML = `<p class="terminal-error">Error: ${error.message}</p>`;
    }
}

function displayQueryBuilderResult(result) {
    const container = document.getElementById('query-result');
    
    if (!result.success) {
        container.innerHTML = `<p class="terminal-error">✗ ${escapeHtml(result.message)}</p>`;
        return;
    }
    
    if (!result.rows || result.rows.length === 0) {
        container.innerHTML = '<p class="loading">No results found</p>';
        return;
    }
    
    container.innerHTML = `
        <table class="data-table">
            <thead>
                <tr>${result.columnNames.map(col => `<th>${col}</th>`).join('')}</tr>
            </thead>
            <tbody>
                ${result.rows.map(row => `
                    <tr>
                        ${result.columnNames.map(col => `<td>${row[col] !== null ? row[col] : 'NULL'}</td>`).join('')}
                    </tr>
                `).join('')}
            </tbody>
        </table>
        <p><strong>${result.rowCount} row(s) returned in ${result.executionTimeMs} ms</strong></p>
    `;
}

// ==================== UTILITY FUNCTIONS ====================

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
