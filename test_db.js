const db = require("./db");

db.query("SELECT * FROM users", (err, results) => {
  if (err) {
    console.error("❌ Query error:", err);
  } else {
    console.log("✅ Users:", results);
  }
  process.exit();
});
