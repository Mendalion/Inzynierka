const { Pool } = require('pg');

const pool = new Pool({
  user: process.env.DB_USER,
  host: process.env.DB_HOST,
  database: process.env.DB_NAME,
  password: process.env.DB_PASSWORD,
  port: process.env.DB_PORT,
});

//test connection
pool.on('connect', () => {
  console.log('Połączono z bazą danych PostgreSQL');
});

pool.on('error', (err) => {
  console.error('Błąd połączenia z bazą:', err);
});

module.exports = {
  query: (text, params) => pool.query(text, params),
  pool
};