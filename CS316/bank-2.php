<?php

require_once 'db_creds.inc';
$isConnected = false;
$mySqlConnection;
$pageName = 'bank-2.php';

// could possibly generalize if planning on supporting multiple acounts
$accountNumber = 0;

try {
    $mySqlConnection = new PDO(K_CONNECTION_STRING, K_USERNAME, K_PASSWORD);
    $mySqlConnection->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $isConnected = true;
} catch (PDOException $e) {
    echo 'Connection failed: ' . $e->getMessage();
}

// call update/transfer functions when buttons are clicked
if (array_key_exists('checkingDepositButton', $_POST)) {
    addToSavings($_POST['checkingDeposit']);
} elseif (array_key_exists('savingsDepositButton', $_POST)) {
    removeFromSavings($_POST['savingsDeposit']);
} elseif (array_key_exists('checkingTransferButton', $_POST)) {
    transferAccountAmount('checking', 'savings', $_POST['checkingTransfer']);
} elseif (array_key_exists('savingsTransferButton', $_POST)) {
    transferAccountAmount('savings', 'checking', $_POST['savingsTransfer']);
}

// wrapper to print echo functionality from return value functionality
function printColumn($columnName)
{
    $columnValue = getColumnValue($columnName);

    if ($columnValue == NULL) {
        echo 'Error loading column: ' . $columnName;
    } else {
        echo $columnValue;
    }
}

function getColumnValue($columnName)
{
    global $isConnected, $mySqlConnection;
    $result = 0;
    if ($isConnected) {
        $checkingQuery = 'SELECT ' . $columnName . ' FROM accounts';
        // kinda hacky? but still works as expected
        $preparedStatement = $mySqlConnection->prepare($checkingQuery);
        $preparedStatement->execute();
        $fetchedValue = $preparedStatement->fetch();
        // see if result is valid
        if ($fetchedValue != false) {
            $result = $fetchedValue[$columnName];
        } else {
            $result = NULL;
        }
    }
    return $result;
}

function addToSavings($checking)
{
    global $isConnected, $mySqlConnection, $pageName;
    if ($checking > 0 && $isConnected) {
        // calculate checking
        $newCheckingAmount = getColumnValue('checking') + $checking;
        // initialize statement
        $updateStatement = $mySqlConnection->prepare('UPDATE accounts set checking = :checking');
        // apply parameters to prepared statement
        $updateStatement->bindParam(':checking', $newCheckingAmount);
        // execute
        $updateStatement->execute();
        header('location:' . $pageName);
    }
}

// 
function removeFromSavings($savings)
{
    global $isConnected, $mySqlConnection, $pageName;
    // validate input
    if ($savings > 0 && $isConnected) {
        // calculate new savings
        $newSavingsAmount = getColumnValue('savings') - $savings;
        // initialize statement
        $updateStatement = $mySqlConnection->prepare('UPDATE accounts set savings = :savings');
        // apply parameters to prepared statement
        $updateStatement->bindParam(':savings', $newSavingsAmount);
        // execute
        $updateStatement->execute();
        header('location:' . $pageName);
    }
}

// remove $amount from the $fromColumnName column and add it to the $toColumnName column
function transferAccountAmount($fromColumnName, $toColumnName, $amount)
{
    global $isConnected, $mySqlConnection, $pageName;
    if ($amount > 0 && $isConnected) {
        // calculate transferred values
        $newFromColumnValue = getColumnValue($fromColumnName) - $amount;
        $newToColumnValue = getColumnValue($toColumnName) + $amount;

        // initialize statement
        $updateStatement = $mySqlConnection->prepare('UPDATE accounts set checking = :checking, savings = :savings');
        // apply parameters based on given column names
        $updateStatement->bindParam(':' . $fromColumnName, $newFromColumnValue);
        $updateStatement->bindParam(':' . $toColumnName, $newToColumnValue);
        // execute
        $updateStatement->execute();
        header('location:' . $pageName);
    }
}
?>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <title>First Bank of HTML&#x2122;</title>

    <style>
        /* Styling for checking account info */
        .checking-account-info {
            border: 2px solid blue;
        }
    </style>
</head>

<!-- Body containing all specified sections of page -->

<body>
    <!-- Welcome header -->
    <h1>Welcome to the First Bank of HTML&#x2122;</h1>
    <p>
        Where all our clients are served!
        <br /><mark>This website is under construction, as is our bank.</mark>
    </p>

    <h2>Services offered</h2>
    <form method='post'>
        <ol type="1">
            <li>
                Current account information
                <table class="checking-account-info">
                    <tr>
                        <th>checking</th>
                        <th>savings</th>
                    </tr>
                    <tr>
                        <td>
                            <?php
                            printColumn('checking');
                            ?>
                        </td>
                        <td>
                            <?php
                            printColumn('savings');
                            ?>
                        </td>
                    </tr>
                </table>
            </li>
            <li>Deposit money into checking <input type="text" name="checkingDeposit"></input> <input type="submit" value="Submit" name="checkingDepositButton"></li>
            <li>Withdraw money into savings <input type="text" name="savingsDeposit"></input> <input type="submit" value="Submit" name="savingsDepositButton"></li>
            <li>Transfer money from checking into savings <input type="text" name="checkingTransfer"></input> <input type="submit" value="Submit" name="checkingTransferButton"></li>
            <li>Transfer money from savings into checking <input type="text" name="savingsTransfer"></input> <input type="submit" value="Submit" name="savingsTransferButton"></li>
        </ol>
    </form>
</body>

</html>