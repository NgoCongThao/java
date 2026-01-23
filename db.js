const mysql = require("mysql2");

const db = mysql.createPool({
  host: "localhost",
  user: "restaurant_user",
  password: "18072005",
  database: "restaurant_db"
});

module.exports = db;