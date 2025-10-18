# 🧪 Complete End-to-End Testing Guide Using Swagger UI

This guide will walk you through testing the entire Jackpot Backend Service workflow using only the Swagger UI interface - no command line tools required!

## 📋 Table of Contents
1. [Prerequisites](#prerequisites)
2. [Starting the Application](#starting-the-application)
3. [Accessing Swagger UI](#accessing-swagger-ui)
4. [Understanding the Workflow](#understanding-the-workflow)
5. [Step-by-Step Testing](#step-by-step-testing)
   - Scenario 1: Basic Bet Submission and Evaluation
   - Scenario 2: Multiple Bets to Grow the Pool
   - Scenario 3: Testing Variable Contribution (Jackpot 2)
   - Scenario 4: Testing Input Validation
   - Scenario 5: Testing Error Handling
   - Scenario 6: Comparing All Three Jackpots
6. [Testing Scenarios Summary](#testing-scenarios-summary)
7. [Troubleshooting](#troubleshooting)

---

## 🔧 Prerequisites

Before you begin, ensure you have:

- ✅ Java 17 or higher installed
- ✅ Maven 3.6+ installed
- ✅ Project built successfully: `mvn clean install`

That's it! No Kafka, no Docker needed for basic testing (mock mode is enabled by default).

---

## 🚀 Starting the Application

### Step 1: Open Terminal

Navigate to the project directory:
```bash
cd /Users/dsolo/dev/Jackpot
```

### Step 2: Start the Application

Run the application using Maven:
```bash
mvn spring-boot:run
```

### Step 3: Wait for Startup

You'll see logs indicating the application is starting. Wait for this line:
```
Started JackpotApplication in X.XXX seconds
```

The application is now running on `http://localhost:8080`

---

## 🌐 Accessing Swagger UI

### Step 1: Open Your Web Browser

Open your preferred browser (Chrome, Firefox, Safari, etc.)

### Step 2: Navigate to Swagger UI

Go to:
```
http://localhost:8080/swagger-ui.html
```

### Step 3: Explore the API

You should see the Swagger UI interface with three main sections:
- **Bet Management** - Submit bets
- **Jackpot Management** - Evaluate rewards and view jackpots
- **Schemas** - Data structures

---

## 📊 Understanding the Workflow

The complete jackpot flow works like this:

```
1. SUBMIT BET → 2. BET PROCESSED → 3. CONTRIBUTION ADDED → 4. EVALUATE REWARD
   (POST /api/bets)   (Kafka/Mock)     (Database Updated)     (GET /api/jackpots/{betId}/evaluate)
```

**What happens behind the scenes:**
1. You submit a bet with amount and jackpot ID
2. System calculates contribution based on jackpot strategy
3. Contribution is added to jackpot pool
4. You can check if the bet won the jackpot reward
5. If won, jackpot resets to initial value

---

## 🧪 Step-by-Step Testing

### 🎯 SCENARIO 1: Basic Bet Submission and Evaluation

This scenario tests the complete flow with Jackpot 1 (Fixed contribution 10%, Fixed reward 5% chance).

---

#### **Step 1: View Initial Jackpot State**

**Purpose:** See the starting pool value before any bets.

1. In Swagger UI, find **"Jackpot Management"** section
2. Click on **`GET /api/jackpots/{jackpotId}`** to expand it
3. Click **"Try it out"** button (top right of the section)
4. In the **jackpotId** field, enter: `1`
5. Click **"Execute"** button (blue button)

**Expected Response:**
```json
{
  "id": 1,
  "initialPoolValue": 1000.00,
  "currentPoolValue": 1000.00,
  "contributionType": "FIXED_CONTRIBUTION",
  "contributionConfig": "{\"percentage\": 0.10}",
  "rewardType": "FIXED_REWARD",
  "rewardConfig": "{\"percentage\": 0.05}"
}
```

📝 **Note:** `currentPoolValue` is 1000.00 - this will increase after we submit bets.

---

#### **Step 2: Submit Your First Bet**

**Purpose:** Send a bet to the system and contribute to the jackpot pool.

1. In Swagger UI, find **"Bet Management"** section
2. Click on **`POST /api/bets`** to expand it
3. Click **"Try it out"** button
4. In the **Request body** field, you'll see example JSON. Replace it with:

```json
{
  "betId": 1001,
  "userId": 2001,
  "jackpotId": 1,
  "betAmount": 100.00
}
```

5. Click **"Execute"** button

**Expected Response:**
- **Status Code:** `202` (Accepted)
- **Response body:** `"Bet published successfully"`

📝 **What happened?**
- Your bet (100.00) was submitted
- 10% contribution = 10.00 added to jackpot pool
- New pool value = 1010.00

---

#### **Step 3: Verify Pool Increased**

**Purpose:** Confirm the contribution was added to the jackpot.

1. Go back to **`GET /api/jackpots/{jackpotId}`** (scroll up)
2. It should still be in "Try it out" mode
3. Enter jackpotId: `1`
4. Click **"Execute"**

**Expected Response:**
```json
{
  "id": 1,
  "initialPoolValue": 1000.00,
  "currentPoolValue": 1010.00,  ← INCREASED!
  ...
}
```

✅ **Success!** The pool increased from 1000.00 to 1010.00

---

#### **Step 4: Evaluate if Bet Won Jackpot**

**Purpose:** Check if your bet wins the jackpot reward.

1. In Swagger UI, find **`GET /api/jackpots/{betId}/evaluate`**
2. Click to expand it
3. Click **"Try it out"**
4. In the **betId** field, enter: `1001` (the betId we used earlier)
5. Click **"Execute"**

**Possible Response 1 - YOU WON! 🎉**
```json
{
  "won": true,
  "rewardAmount": 1010.00,
  "message": "Congratulations! You won the jackpot!"
}
```

**Possible Response 2 - You Didn't Win 😞**
```json
{
  "won": false,
  "rewardAmount": 0.0,
  "message": "Better luck next time!"
}
```

📝 **Note:** Jackpot 1 has a 5% win chance, so you'll likely need to try multiple times.

---

#### **Step 5: If You Won - Verify Jackpot Reset**

**Only if you won in Step 4:**

1. Go back to **`GET /api/jackpots/{jackpotId}`**
2. Enter jackpotId: `1`
3. Click **"Execute"**

**Expected Response:**
```json
{
  "id": 1,
  "currentPoolValue": 1000.00,  ← RESET TO INITIAL!
  ...
}
```

✅ **Confirmed!** Jackpot was reset after winning.

---

### 🎯 SCENARIO 2: Multiple Bets to Grow the Pool

Let's submit multiple bets to see the pool grow significantly!

---

#### **Step 1: Submit Bet #2**

```json
{
  "betId": 1002,
  "userId": 2002,
  "jackpotId": 1,
  "betAmount": 200.00
}
```

Contribution: 200 × 10% = **20.00**

---

#### **Step 2: Submit Bet #3**

```json
{
  "betId": 1003,
  "userId": 2003,
  "jackpotId": 1,
  "betAmount": 150.00
}
```

Contribution: 150 × 10% = **15.00**

---

#### **Step 3: Submit Bet #4**

```json
{
  "betId": 1004,
  "userId": 2004,
  "jackpotId": 1,
  "betAmount": 300.00
}
```

Contribution: 300 × 10% = **30.00**

---

#### **Step 4: Check Total Pool**

Use **`GET /api/jackpots/1`**

**Expected currentPoolValue:**
- Started at: 1000.00
- Bet #1 added: +10.00 = 1010.00
- Bet #2 added: +20.00 = 1030.00
- Bet #3 added: +15.00 = 1045.00
- Bet #4 added: +30.00 = **1075.00**

---

#### **Step 5: Evaluate Each Bet**

Try evaluating each bet ID to see if any won:
- Evaluate bet 1002
- Evaluate bet 1003
- Evaluate bet 1004

Each has a 5% chance to win the current pool!

---

### 🎯 SCENARIO 3: Testing Variable Contribution (Jackpot 2)

Jackpot 2 uses **variable contribution** - it starts at 15% but decreases as the pool grows.

---

#### **Step 1: Check Initial State of Jackpot 2**

Use **`GET /api/jackpots/2`**

**Expected Response:**
```json
{
  "id": 2,
  "initialPoolValue": 5000.00,
  "currentPoolValue": 5000.00,
  "contributionType": "VARIABLE_CONTRIBUTION",
  "contributionConfig": "{\"basePercentage\": 0.15, \"poolLimit\": 10000.00}",
  "rewardType": "VARIABLE_REWARD",
  "rewardConfig": "{\"poolLimit\": 10000.00}"
}
```

📝 **Key points:**
- Base contribution: 15%
- Pool limit: 10,000
- Contribution decreases as pool approaches 10,000
- Win chance increases as pool approaches 10,000

---

#### **Step 2: Submit Small Bet to Jackpot 2**

```json
{
  "betId": 2001,
  "userId": 3001,
  "jackpotId": 2,
  "betAmount": 100.00
}
```

**Expected Contribution:**
- Formula: `100 × 0.15 × (1 - 5000/10000)`
- = `100 × 0.15 × 0.5`
- = **7.50**

---

#### **Step 3: Verify the Calculation**

Use **`GET /api/jackpots/2`**

**Expected currentPoolValue:** 5007.50

---

#### **Step 4: Submit Large Bet When Pool is Near Limit**

Let's submit several more bets to get close to the 10,000 limit:

```json
{
  "betId": 2002,
  "userId": 3002,
  "jackpotId": 2,
  "betAmount": 1000.00
}
```

Then:
```json
{
  "betId": 2003,
  "userId": 3003,
  "jackpotId": 2,
  "betAmount": 1000.00
}
```

Keep adding bets until the pool is close to 10,000.

---

#### **Step 5: Evaluate for Near-Guaranteed Win**

When the pool is close to 10,000 (like 9,500+), evaluate a bet:

Use **`GET /api/jackpots/{betId}/evaluate`** with one of your recent bet IDs.

**Win chance formula:** `currentPool / 10000`
- If pool = 9,500, win chance = 95%!
- If pool = 10,000, win chance = 100% (guaranteed!)

---

### 🎯 SCENARIO 4: Testing Input Validation

Test the comprehensive input validation system.

---

#### **Test 1: Negative Bet ID**

Submit a bet with a negative bet ID:

```json
{
  "betId": -123,
  "userId": 1001,
  "jackpotId": 1,
  "betAmount": 100.00
}
```

**Expected Response:**
- **Status Code:** `400` (Bad Request)
- **Response body:**
```json
{
  "error": "Validation failed",
  "status": 400,
  "timestamp": "2025-10-18T21:44:07.317",
  "details": {
    "betId": "Bet ID must be positive"
  }
}
```

✅ **Good!** System validates that bet IDs must be positive.

---

#### **Test 2: Negative Bet Amount**

Submit a bet with a negative amount:

```json
{
  "betId": 1001,
  "userId": 1001,
  "jackpotId": 1,
  "betAmount": -50.00
}
```

**Expected Response:**
- **Status Code:** `400` (Bad Request)
- **Response body:**
```json
{
  "error": "Validation failed",
  "status": 400,
  "timestamp": "2025-10-18T21:44:07.317",
  "details": {
    "betAmount": "Bet amount must be at least 0.01"
  }
}
```

✅ **Good!** System validates that bet amounts must be positive and at least 0.01.

---

#### **Test 3: Bet Amount Too Small**

Submit a bet with amount less than minimum:

```json
{
  "betId": 1001,
  "userId": 1001,
  "jackpotId": 1,
  "betAmount": 0.001
}
```

**Expected Response:**
- **Status Code:** `400` (Bad Request)
- **Response body:**
```json
{
  "error": "Validation failed",
  "status": 400,
  "details": {
    "betAmount": "Bet amount must be at least 0.01"
  }
}
```

✅ **Good!** Minimum bet amount is enforced.

---

#### **Test 4: Multiple Validation Errors**

Submit a bet with multiple invalid fields:

```json
{
  "betId": -123,
  "userId": 0,
  "jackpotId": -5,
  "betAmount": -100.00
}
```

**Expected Response:**
- **Status Code:** `400` (Bad Request)
- **Response body:**
```json
{
  "error": "Validation failed",
  "status": 400,
  "details": {
    "betId": "Bet ID must be positive",
    "userId": "User ID must be positive",
    "jackpotId": "Jackpot ID must be positive",
    "betAmount": "Bet amount must be at least 0.01"
  }
}
```

✅ **Good!** System reports all validation errors at once.

---

#### **Test 5: Missing Required Fields**

Submit a bet with null values:

```json
{
  "betId": null,
  "userId": 1001,
  "jackpotId": 1,
  "betAmount": 100.00
}
```

**Expected Response:**
- **Status Code:** `400` (Bad Request)
- **Response body:**
```json
{
  "error": "Validation failed",
  "status": 400,
  "details": {
    "betId": "Bet ID is required"
  }
}
```

✅ **Good!** All required fields are validated.

---

### 🎯 SCENARIO 5: Testing Error Handling

Test various error scenarios with proper HTTP status codes.

---

#### **Test 1: Non-Existent Jackpot**

Submit a bet to a jackpot that doesn't exist:

```json
{
  "betId": 9001,
  "userId": 9001,
  "jackpotId": 999,
  "betAmount": 100.00
}
```

**Expected Response:**
- **Status Code:** `500` (Internal Server Error)
- **Response body:**
```json
{
  "error": "Internal server error",
  "status": 500,
  "timestamp": "2025-10-18T21:54:36.722",
  "message": "Jackpot not found with ID: 999"
}
```

✅ **Good!** System validates jackpot exists before processing.

---

#### **Test 2: Evaluating Non-Existent Bet**

Try to evaluate a bet that was never submitted:

Use **`GET /api/jackpots/99999/evaluate`**

**Expected Response:**
- **Status Code:** `404` (Not Found)
- **Response body:**
```json
{
  "error": "Bet not found",
  "status": 404,
  "timestamp": "2025-10-18T21:54:36.981",
  "message": "No contribution found for bet ID: 99999. The bet may not have been processed yet or does not exist."
}
```

✅ **Good!** Clear 404 error when bet doesn't exist.

---

#### **Test 3: Invalid Bet ID Type in Evaluation**

Try to evaluate with a non-numeric bet ID:

Use **`GET /api/jackpots/abc/evaluate`**

**Expected Response:**
- **Status Code:** `400` (Bad Request)
- **Response body:**
```json
{
  "error": "Invalid parameter type",
  "status": 400,
  "timestamp": "2025-10-18T21:54:36.884",
  "message": "Parameter 'betId' must be a valid Long"
}
```

✅ **Good!** Type mismatch errors are handled properly.

---

#### **Test 4: Negative Bet ID in Evaluation**

Try to evaluate with a negative bet ID:

Use **`GET /api/jackpots/-123/evaluate`**

**Expected Response:**
- **Status Code:** `400` (Bad Request)
- **Response body:**
```json
{
  "error": "Invalid bet ID",
  "status": 400,
  "timestamp": "2025-10-18T21:54:36.989",
  "message": "Invalid bet ID: -123. Bet ID must be a positive number."
}
```

✅ **Good!** Negative bet IDs are rejected.

---

#### **Test 5: Zero Bet ID in Evaluation**

Try to evaluate with bet ID of zero:

Use **`GET /api/jackpots/0/evaluate`**

**Expected Response:**
- **Status Code:** `400` (Bad Request)
- **Response body:**
```json
{
  "error": "Invalid bet ID",
  "status": 400,
  "timestamp": "2025-10-18T21:54:36.984",
  "message": "Invalid bet ID: 0. Bet ID must be a positive number."
}
```

✅ **Good!** Zero is not a valid bet ID.

---

#### **Test 6: Non-Existent Jackpot Retrieval**

Try to get details of a jackpot that doesn't exist:

Use **`GET /api/jackpots/999999`**

**Expected Response:**
- **Status Code:** `404` (Not Found)
- **Response body:**
```json
{
  "error": "Jackpot not found",
  "status": 404,
  "timestamp": "2025-10-18T21:54:36.936",
  "message": "Jackpot not found with ID: 999999"
}
```

✅ **Good!** Clear 404 error when jackpot doesn't exist.

---

#### **Test 7: Invalid Jackpot ID Type**

Try to get jackpot with non-numeric ID:

Use **`GET /api/jackpots/xyz`**

**Expected Response:**
- **Status Code:** `400` (Bad Request)
- **Response body:**
```json
{
  "error": "Invalid parameter type",
  "status": 400,
  "timestamp": "2025-10-18T21:54:36.986",
  "message": "Parameter 'jackpotId' must be a valid Long"
}
```

✅ **Good!** Type validation works for all endpoints.

---

### 🎯 SCENARIO 6: Comparing All Three Jackpots

Submit identical bets to each jackpot and compare contributions.

---

#### **Submit 100.00 to Each Jackpot**

**Jackpot 1:**
```json
{
  "betId": 5001,
  "userId": 4001,
  "jackpotId": 1,
  "betAmount": 100.00
}
```

**Jackpot 2:**
```json
{
  "betId": 5002,
  "userId": 4002,
  "jackpotId": 2,
  "betAmount": 100.00
}
```

**Jackpot 3:**
```json
{
  "betId": 5003,
  "userId": 4003,
  "jackpotId": 3,
  "betAmount": 100.00
}
```

---

#### **Check All Three Pools**

Use **`GET /api/jackpots/{id}`** for each:

**Jackpot 1:**
- Contribution: 10% of 100 = **10.00 fixed**
- Win chance: **5% fixed**

**Jackpot 2:**
- Contribution: **Variable** (depends on current pool)
- Win chance: **Variable** (increases with pool)

**Jackpot 3:**
- Contribution: 5% of 100 = **5.00 fixed**
- Win chance: **10% fixed**

📊 **Comparison:**
| Jackpot | Contribution | Win Chance |
|---------|-------------|------------|
| 1 | 10.00 | 5% |
| 2 | ~7.50 | ~50% |
| 3 | 5.00 | 10% |

---

## 🎲 Testing Scenarios Summary

### Quick Test Checklist

Use this checklist to verify all functionality:

- [ ] **Basic Flow**
  - [ ] View jackpot details
  - [ ] Submit a bet
  - [ ] Verify pool increased
  - [ ] Evaluate bet for reward

- [ ] **Multiple Bets**
  - [ ] Submit 5+ bets to same jackpot
  - [ ] Verify pool grows with each bet
  - [ ] Evaluate multiple bets

- [ ] **Variable Strategies**
  - [ ] Test variable contribution (Jackpot 2)
  - [ ] Test variable reward (Jackpot 2)
  - [ ] Get pool close to limit (10,000)
  - [ ] Verify high win probability

- [ ] **Jackpot Reset**
  - [ ] Win a jackpot
  - [ ] Verify pool reset to initial value
  - [ ] Submit new bet to reset jackpot

- [ ] **Input Validation**
  - [ ] Test negative bet ID
  - [ ] Test negative bet amount
  - [ ] Test bet amount below minimum (0.01)
  - [ ] Test multiple validation errors
  - [ ] Test missing required fields
  - [ ] Verify 400 Bad Request responses

- [ ] **Error Handling**
  - [ ] Submit to non-existent jackpot (500)
  - [ ] Evaluate non-existent bet (404)
  - [ ] Test invalid bet ID types (400)
  - [ ] Test negative/zero bet IDs in evaluation (400)
  - [ ] Test non-existent jackpot retrieval (404)
  - [ ] Verify proper HTTP status codes

---

## 🎯 Expected Test Results

### Jackpot 1 (Fixed 10% / 5%)
- **100.00 bet** → Pool increases by **10.00**
- Win probability: **5%** (1 in 20 bets on average)
- Pool growth: **Linear and predictable**

### Jackpot 2 (Variable / Variable)
- **100.00 bet at 5000 pool** → Pool increases by **~7.50**
- **100.00 bet at 9000 pool** → Pool increases by **~1.50**
- Win probability: **Increases with pool size**
- At 10,000 pool: **100% guaranteed win**

### Jackpot 3 (Fixed 5% / 10%)
- **100.00 bet** → Pool increases by **5.00**
- Win probability: **10%** (1 in 10 bets on average)
- **Smaller contributions, higher win chance**

---

## 🔍 Advanced Testing Tips

### Tip 1: Calculate Expected Pool Values

Before clicking Execute, calculate what you expect:

**For Fixed Contribution:**
```
New Pool = Current Pool + (Bet Amount × Percentage)
```

**For Variable Contribution:**
```
Contribution = Bet Amount × Base% × (1 - Current Pool / Pool Limit)
```

### Tip 2: Test Until You Win

Keep evaluating the same bet ID multiple times - each evaluation is a new random check!

For 5% chance jackpot: Expect to win within ~20 evaluations on average.

### Tip 3: Force a Win on Jackpot 2

Submit enough bets to get pool to exactly 10,000:

1. Check current pool
2. Calculate: `(10000 - current) / 0.15 = bet amount needed`
3. Submit that bet
4. Evaluate - **Guaranteed win!**

### Tip 4: Monitor with H2 Console

While testing, you can view the database:

1. Go to `http://localhost:8080/h2-console`
2. JDBC URL: `jdbc:h2:mem:jackpotdb`
3. Username: `sa`
4. Password: (leave empty)

Run queries:
```sql
-- See all contributions
SELECT * FROM jackpot_contribution ORDER BY created_at DESC;

-- See all rewards
SELECT * FROM jackpot_reward ORDER BY created_at DESC;

-- Check current pool values
SELECT id, initial_pool_value, current_pool_value FROM jackpot;
```

---

## 🐛 Troubleshooting

### Problem: Swagger UI Not Loading

**Solution:**
1. Check application is running: Look for "Started JackpotApplication"
2. Try alternate URL: `http://localhost:8080/swagger-ui/index.html`
3. Clear browser cache and refresh
4. Check port 8080 is not in use by another app

---

### Problem: "Bet published successfully" but Pool Didn't Increase

**Reason:** You're in **mock mode** - bets are processed synchronously.

**Check:**
1. Wait 1-2 seconds after submitting
2. Refresh the GET jackpot call
3. Pool should have increased

**If still not working:**
- Check application logs for errors
- Verify jackpotId matches an existing jackpot (1, 2, or 3)

---

### Problem: Getting 400 Bad Request on Bet Submission

**Common Causes:**
1. **Negative values** - All IDs and amounts must be positive
2. **Bet amount too small** - Minimum is 0.01
3. **Missing required fields** - All 4 fields (betId, userId, jackpotId, betAmount) required
4. **Invalid JSON** - Check syntax in request body

**Example of correct JSON:**
```json
{
  "betId": 1001,
  "userId": 2001,
  "jackpotId": 1,
  "betAmount": 100.00
}
```

**Validation Rules:**
- `betId`: Must be positive (> 0)
- `userId`: Must be positive (> 0)
- `jackpotId`: Must be positive (> 0)
- `betAmount`: Must be at least 0.01

---

### Problem: Getting 500 Error on Bet Submission

**Common Causes:**
1. **Invalid jackpotId** - Use 1, 2, or 3 only (valid jackpot IDs)
2. **Server error** - Check application logs for details

**Note:** 400 errors are for invalid input, 500 errors are for server-side issues like non-existent jackpot.

---

### Problem: Never Winning with Jackpot 1 (5% chance)

**This is normal!** 5% = 1 in 20 chance.

**Solutions:**
- Try evaluating 20-30 times
- Switch to Jackpot 3 (10% chance - easier to win)
- Use Jackpot 2 with high pool for guaranteed win

---

### Problem: Can't Find Executed Bet

**Solution:**
- Note down the `betId` you used when submitting
- Use that exact number in the evaluate endpoint
- Bet IDs must be unique - don't reuse numbers

---

## 📝 Example Test Session

Here's a complete test session you can copy/paste:

### Session Goal: Test complete flow and win a jackpot

```
1. GET /api/jackpots/3
   → Note current pool (should be 2000.00)

2. POST /api/bets
   {
     "betId": 7001,
     "userId": 8001,
     "jackpotId": 3,
     "betAmount": 200.00
   }
   → Should return "Bet published successfully"

3. GET /api/jackpots/3
   → Pool should now be 2010.00 (2000 + 10)

4. GET /api/jackpots/7001/evaluate
   → 10% chance to win
   → If lost, try again! (Click Execute multiple times)
   → Keep trying until you win!

5. GET /api/jackpots/3
   → Pool should be reset to 2000.00

6. Repeat steps 2-5 with different betId numbers!
```

---

## 🎓 Learning Outcomes

After completing this guide, you'll understand:

✅ How to use Swagger UI for API testing
✅ How the jackpot contribution system works
✅ Difference between fixed and variable strategies
✅ How jackpot rewards are evaluated
✅ How jackpots reset after winning
✅ How input validation protects the system
✅ How to test error scenarios with proper HTTP status codes
✅ Difference between 400 (client error) and 404/500 (server errors)
✅ How probability affects reward chances

---

## 📚 Additional Resources

- **Full API Documentation:** Check the README.md
- **Input Validation Guide:** See INPUT_VALIDATION.md for complete validation rules
- **Error Handling Guide:** See ERROR_HANDLING.md for all error scenarios
- **Database Schema:** See CLAUDE.md
- **Source Code:** Explore `src/main/java/com/jackpot/`
- **Tests:** See `src/test/java/` for more examples

---

## 🎉 Happy Testing!

You now have everything you need to fully test the Jackpot Backend Service using only Swagger UI. Try different scenarios, experiment with different bet amounts, and see how the strategies affect contributions and rewards!

**Pro tip:** Keep the H2 console open in another tab to watch the database change in real-time as you submit bets!

---

**Need help?** Check the troubleshooting section or review the application logs while testing.
