<?php

require_once 'db_creds.inc';
$isConnected = false;
$mySqlConnection;
$pageName = 'bank-2.php';

// request parameters

// could possibly generalize if planning on supporting multiple acounts
$accountNumber = 0;

try {
    $mySqlConnection = new PDO(K_CONNECTION_STRING, K_USERNAME, K_PASSWORD);
    $mySqlConnection->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $isConnected = true;
} catch (PDOException $e) {
    echo 'Connection failed: ' . $e->getMessage();
}

// check for request parameters
if (isset($_POST['checkingDeposit']) && $_POST['checkingDeposit']) {
    addToSavings($_POST['checkingDeposit']);
} elseif (isset($_POST['savingsDeposit']) && $_POST['savingsDeposit']) {
    removeFromSavings($_POST['savingsDeposit']);
} elseif (isset($_POST['checkingTransfer']) && $_POST['checkingTransfer']) {
    transferAccountAmount('checking', 'savings', $_POST['checkingTransfer']);
} elseif (isset($_POST['savingsTransfer']) && $_POST['savingsTransfer']) {
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
    global $isConnected, $mySqlConnection;

    $newCheckingAmount = 0;

    // validate input
    if ($checking > 0 && $isConnected) {
        // calculate checking
        $newCheckingAmount = getColumnValue('checking') + $checking;
        // initialize statement
        $updateStatement = $mySqlConnection->prepare('UPDATE accounts set checking = :checking');
        // apply parameters to prepared statement
        $updateStatement->bindParam(':checking', $newCheckingAmount);
        // execute
        $updateStatement->execute();
    }
    $response = array('checkingDeposit' => $newCheckingAmount);
    echo json_encode($response);
    return $newCheckingAmount;
}

function removeFromSavings($savings)
{
    global $isConnected, $mySqlConnection;

    $newSavingsAmount = 0;

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
    }
    $response = array('savingsDeposit' => $newSavingsAmount);
    echo json_encode($response);
    return $newSavingsAmount;
}

// remove $amount from the $fromColumnName column and add it to the $toColumnName column
function transferAccountAmount($fromColumnName, $toColumnName, $amount)
{
    global $isConnected, $mySqlConnection;



    // validate input
    if ($amount > 0 && $amount < getColumnValue($fromColumnName) && $isConnected) {
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
    }
    // make sure that the JSON data is compiled in the correct order
    if ($fromColumnName == 'checking') {
        $response = array('checking' => $newFromColumnValue, 'savings' => $newToColumnValue);
    } else {
        $response = array('checking' => $newToColumnValue, 'savings' => $newFromColumnValue);
    }
    echo json_encode($response);
}

/** Prevent whole page from being sent as a response to allow for pure JSON responses */
if (!isset($_POST['checkingDeposit']) && !isset($_POST['savingsDeposit']) && !isset($_POST['checkingTransfer']) && !isset($_POST['savingsTransfer'])) {
?>

    <!-- ================================================= HTML START ================================================== -->

    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <title>First Bank of HTML&#x2122;</title>
        <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    </head>

    <!-- Body containing all specified sections of page -->

    <body onload='applyStyling()'>

        <!-- javascript for page -->
        <script>
            // apply styling once page is loaded
            function applyStyling() {
                /* Styling for checking account info */
                $('.checking-account-info').css('border', '2px solid blue');
            }

            /** Returns true/false, depending on if value is valid */
            function validateDepositValue(_depositValue) {
                var isValid = (parseFloat(_depositValue).toFixed(2) > 0);
                return isValid;
            }

            /** Returns true/false, depending on if value is valid */
            function validateTransferValue(_transferValue, _accountValue) {
                var isValid = (parseFloat(_transferValue) < parseFloat(_accountValue));
                return isValid;
            }

            /** Build reqeust object for depositing */
            function buildDepositRequest(serializedData) {
                return $.ajax({
                    type: 'POST',
                    dataType: 'json',
                    data: serializedData,
                });
            }

            /** update html for updated table fields */
            function updateChecking(_newCheckingAmount) {
                $('#checkingInfo').html(parseFloat(_newCheckingAmount).toFixed(2));
            }

            function updateSavings(_newSavingsAmount) {
                $('#savingsInfo').html(parseFloat(_newSavingsAmount).toFixed(2));
            }

            /** Remove values from all inputs */
            function clearInputFields() {
                $('#checkingDeposit').val('');
                $('#savingsDeposit').val('');
                $('#checkingTransfer').val('');
                $('#savingsTransfer').val('');
            }

            /** Create and perform request for deposit/withdrawal */
            function updateDepositValue(depositField) {
                var fieldId = '#' + depositField;
                var buttonFieldId = fieldId + 'Button';
                var depositValue = $(fieldId).val();
                var serializedData = {};

                if (validateDepositValue(depositValue)) {
                    $(buttonFieldId).prop('disabled', true);
                    serializedData[depositField] = depositValue;
                    var request = buildDepositRequest(serializedData);

                    request.done(function(response) {
                        if (depositField == 'checkingDeposit') {
                            updateChecking(response[depositField]);
                        } else {
                            updateSavings(response[depositField]);
                        }
                        $(buttonFieldId).prop('disabled', false);
                        clearInputFields();
                    });

                    request.error(function(xhr, status, error) {
                        alert('STATUS: ' + status + '  ERROR: ' + error);
                    });

                    request.fail(function() {
                        alert('Request failed');
                    });
                } else {
                    alert("Input is not valid, please enter a positive number.")
                }
            }

            /** Create and perform request for transfer */
            function updateTransferValue(transferField) {
                var fieldId = '#' + transferField + 'Transfer';
                var buttonFieldId = fieldId + 'Button';
                var transferValue = $(fieldId).val();
                var accountValue = $('#' + transferField + 'Info').html().trim();
                var serializedData = {};

                if (validateTransferValue(transferValue, accountValue)) {
                    $(buttonFieldId).prop('disabled', true);
                    serializedData[transferField + 'Transfer'] = transferValue;
                    var request = buildDepositRequest(serializedData);

                    request.done(function(response) {
                        updateChecking(response['checking']);
                        updateSavings(response['savings']);
                        $(buttonFieldId).prop('disabled', false);
                        clearInputFields();
                    });
                } else {
                    alert("Input is not valid, please enter a positive number that is within your checkings and/or savings.")
                }
            }


            $(document).ready(function() {

                /* Button click calls */
                $('#checkingDepositButton').click(function() { updateDepositValue('checkingDeposit') });
                $('#savingsDepositButton').click(function() { updateDepositValue('savingsDeposit') });
                $('#checkingTransferButton').click(function() { updateTransferValue('checking') });
                $('#savingsTransferButton').click(function() { updateTransferValue('savings') });

                /** EXTRA CREDIT MATERIAL */
                $('h1').click(function() {
                    alert("Just trying to get ahead in life. (Get it? Because you clicked on a header? ... OK I'll stop now.)")
                });
            });
        </script>

        <!-- Welcome header -->
        <h1>Welcome to the First Bank of HTML&#x2122;</h1>
        <p>
            Where all our clients are served!
            <br /><mark>This website is under construction, as is our bank.</mark>
        </p>

        <h2>Services offered</h2>
        <ol type="1">
            <li>
                Current account information
                <table class="checking-account-info">
                    <tr>
                        <th>checking</th>
                        <th>savings</th>
                    </tr>
                    <tr>
                        <td id='checkingInfo'>
                            <?php
                            printColumn('checking');
                            ?>
                        </td>
                        <td id='savingsInfo'>
                            <?php
                            printColumn('savings');
                            ?>
                        </td>
                    </tr>
                </table>
            </li>
            <li>Deposit money into checking <input type="text" id="checkingDeposit"> <button type='button' id="checkingDepositButton">submit</button></li>
            <li>Withdraw money from savings <input type="text" id="savingsDeposit"> <button type='button' id="savingsDepositButton">submit</button></li>
            <li>Transfer money from checking into savings <input type="text" id="checkingTransfer"> <button type='button' id="checkingTransferButton">submit</button></li>
            <li>Transfer money from savings into checking <input type="text" id="savingsTransfer"> <button type='button' id="savingsTransferButton">submit</button></li>
        </ol>
    </body>

    </html>

<?php
}
?>