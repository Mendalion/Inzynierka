const express = require('express');
const dotenv = require('dotenv');
const db = require('./src/config/database');

dotenv.config();
const app = express();

app.use(express.json());

// testowe połączenie z bazą
app.get('/health', async (req, res) => {
  try {
    const result = await db.query('SELECT NOW()');
    res.json({ status: 'OK', dbTime: result.rows[0].now });
  } catch (error) {
    console.error('Błąd testu bazy:', error);
    res.status(500).json({ status: 'ERROR', error: error.message });
  }
});

// domyślna trasa
app.get('/', (req, res) => {
  res.send('Serwer backend działa poprawnie 🚀');
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`✅ Serwer działa na porcie ${PORT}`);
});
