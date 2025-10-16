const express = require('express');
const cors = require('cors');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 5000;

//middleware
app.use(cors());
app.use(express.json());

//endpoint test
app.get('/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    message: 'Backend działa poprawnie',
    timestamp: new Date().toISOString()
  });
});

//start serwera
app.listen(PORT, () => {
  console.log(`serwer działa na porcie ${PORT}`);
  console.log(`environment: ${process.env.NODE_ENV || 'development'}`);
});