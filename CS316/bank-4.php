<?php
// run locally with `php -S localhost:8080 bank-4.php`

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

// check for post request parameters
if (isset($_POST['checkingDeposit']) && $_POST['checkingDeposit']) {
    addToSavings($_POST['checkingDeposit']);
} elseif (isset($_POST['savingsDeposit']) && $_POST['savingsDeposit']) {
    removeFromSavings($_POST['savingsDeposit']);
} elseif (isset($_POST['checkingTransfer']) && $_POST['checkingTransfer']) {
    transferAccountAmount('checking', 'savings', $_POST['checkingTransfer']);
} elseif (isset($_POST['savingsTransfer']) && $_POST['savingsTransfer']) {
    transferAccountAmount('savings', 'checking', $_POST['savingsTransfer']);
}

// check for get request parameters
if (isset($_GET['checkingInfo']) && $_GET['checkingInfo']) {
    getColumnValue('checking', true);
} elseif (isset($_GET['savingsInfo']) && $_GET['savingsInfo']) {
    getColumnValue('savings', true);
}

$hasPost = isset($_POST['checkingDeposit']) || isset($_POST['savingsDeposit']) || isset($_POST['checkingTransfer']) || isset($_POST['savingsTransfer']);
$hasGet = isset($_GET['checkingInfo']) || isset($_GET['savingsInfo']);

// wrapper to print echo functionality from return value functionality
function printColumn($columnName)
{
    $columnValue = getColumnValue($columnName, false);

    if ($columnValue == NULL) {
        echo 'Error loading column: ' . $columnName;
    } else {
        echo $columnValue;
    }
}

function getColumnValue($columnName, $getResponse)
{
    global $isConnected, $mySqlConnection;
    $result = 0;
    $response = null;
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
    if ($getResponse) {
        $response = array($columnName . 'Amount' => $result);
        echo json_encode($response);
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
        $newCheckingAmount = getColumnValue('checking', false) + $checking;
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
        $newSavingsAmount = getColumnValue('savings', false) - $savings;
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
    if ($amount > 0 && $amount < getColumnValue($fromColumnName, false) && $isConnected) {
        // calculate transferred values
        $newFromColumnValue = getColumnValue($fromColumnName, false) - $amount;
        $newToColumnValue = getColumnValue($toColumnName, false) + $amount;

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
if (!$hasPost && !$hasGet) {
?>

    <!-- ================================================= HTML START ================================================== -->

    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <title>First Bank of HTML&#x2122;</title>
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
        <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    </head>

    <!-- Initialize navbar -->
    <nav class="navbar sticky-top navbar-expand-md navbar-light" style='background-color: rgb(255,210,255)'>
        <span>Current checking account: <span class='checkingInfo'></span> </span>
        <span>Current savings account: <span class='savingsInfo'></span> </span>
    </nav>

    <!-- Body containing all specified sections of page -->

    <body onload='init()'>

        <!-- javascript for page -->
        <script>
            /** Variables for reading account values */

            checkingAccountAmount = 0;
            savingsAccountAmount = 0;

            /** apply styling once page is loaded */
            function init() {
                // styling for specific elements
                $('body').css('padding-bottom', '200px');
                $('h2').css('padding-top', '50px');

                $('h4').css('padding-top', '30px');
                $('h4').css('padding-bottom', '15px');

                $('button').css('margin', '4px 3px');

                // Styling for checking account info

                $('.checking-account-info').css('border', '2px solid blue');
                $('.checkingInfo').css('border', '2px solid blue');
                $('.savingsInfo').css('border', '2px solid blue');
                $('.checkingInfo').css('padding', '2px 5px');
                $('.savingsInfo').css('padding', '2px 5px');

                // Get checking and savings values
                getCheckingAmount();
                getSavingsAmount();
            }

            /** Returns true/false depending on if input value is positive or negative */
            function inputIsValid(_buttonId) {
                var buttonValue = parseFloat($("#" + _buttonId).val());
                return buttonValue > 0;
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

            /** Build reqeust object for getting value of field */
            function buildGetRequest(requestParams) {
                return $.ajax({
                    type: 'GET',
                    data: requestParams,
                    dataType: 'json',
                });
            }

            /** update html for updated table fields */
            function updateChecking(_newCheckingAmount) {
                $('.checkingInfo').html(parseFloat(_newCheckingAmount).toFixed(2));
            }

            function updateSavings(_newSavingsAmount) {
                $('.savingsInfo').html(parseFloat(_newSavingsAmount).toFixed(2));
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

                    // request.error(function(xhr, status, error) {
                    //     alert('STATUS: ' + status + '  ERROR: ' + error);
                    // });

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
                var accountValue = $('.' + transferField + 'Info').html().trim();
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


            // JS functions to call out to database and get current values of fields
            function getCheckingAmount() {
                var requestParams = {
                    checkingInfo: true
                };
                var request = buildGetRequest(requestParams);

                request.done(function(response) {
                    checkingAccountAmount = response['checkingAmount'];
                    updateChecking(checkingAccountAmount);
                });
            }

            function getSavingsAmount() {
                var requestParams = {
                    savingsInfo: true
                };
                var request = buildGetRequest(requestParams);

                request.done(function(response) {
                    savingsAccountAmount = response['savingsAmount'];
                    updateSavings(savingsAccountAmount);
                });
            }

            function calculateInteretMonthlyPayment(_loanAmount, _yearsToPay, _yearlyInterest) {
                var monthlyRate = _yearlyInterest / 12;
                var c = (1 + monthlyRate / 100) ^ (12 * _yearsToPay);
                var monthlyPayment = _loanAmount * (monthlyRate / 100) * (c / (c - 1));

                return Math.round(monthlyPayment * 100) / 100;
            }

            $(document).ready(function() {

                /* Button click calls */
                $('#checkingDepositButton').click(function() {
                    updateDepositValue('checkingDeposit');
                });
                $('#savingsDepositButton').click(function() {
                    updateDepositValue('savingsDeposit');
                });
                $('#checkingTransferButton').click(function() {
                    updateTransferValue('checking');
                });
                $('#savingsTransferButton').click(function() {
                    updateTransferValue('savings');
                });
                $('#loanCalculateButton').click(function() {
                    // get values from inputs then call loan function
                    var principal = parseFloat($('#principalField').val());
                    var years = parseInt($('#yearsField').val());
                    var interestRate = parseFloat($('#rangeval').html());
                    if (!isNaN(principal) && !isNaN(years) && !isNaN(interestRate))
                        $('#monthlyPaymentField').html(calculateInteretMonthlyPayment(principal, years, interestRate));
                    else
                        alert('Verify loan inputs are numeric.');
                });

                /** EXTRA CREDIT MATERIAL */
                $('h1').click(function() {
                    alert(
                        "Just trying to get ahead in life. (Get it? Because you clicked on a header? ... OK I'll stop now.)")
                });

                $('#checkingDeposit').keyup(function() {
                    var valid = inputIsValid('checkingDeposit');
                    if (valid) {
                        $('#checkingDepositButton').removeClass('disabled');
                    } else {
                        $('#checkingDepositButton').addClass('disabled');
                    }
                });

                $('#savingsDeposit').keyup(function() {
                    var valid = inputIsValid('savingsDeposit');
                    if (valid) {
                        $('#savingsDepositButton').removeClass('disabled');
                    } else {
                        $('#savingsDepositButton').addClass('disabled');
                    }
                });

                $('#checkingTransfer').keyup(function() {
                    var valid = inputIsValid('checkingTransfer');
                    if (valid) {
                        $('#checkingTransferButton').removeClass('disabled');
                    } else {
                        $('#checkingTransferButton').addClass('disabled');
                    }
                });

                $('#savingsTransfer').keyup(function() {
                    var valid = inputIsValid('savingsTransfer');
                    if (valid) {
                        $('#savingsTransferButton').removeClass('disabled');
                    } else {
                        $('#savingsTransferButton').addClass('disabled');
                    }
                });

            });
        </script>
        <div class="container-fluid">

            <!-- Welcome header -->
            <h1>Welcome to the First Bank of HTML&#x2122;</h1>
            <p>
                Where all our clients are served!
                <br /><mark>This website is under construction, as is our bank.</mark>
            </p>

            <h4>Account Information: </h4>
            <div class="col-8 col-sm-3">
                <table class="table table-bordered">
                    <thead>
                        <tr>
                            <th><span data-toggle="tooltip" data-placement="top" title="Column sorted by account number">Account #</span></th>
                            <th><span data-toggle="tooltip" data-placement="top" title="Column sorted by checking amount">Checking</span></th>
                            <th><span data-toggle="tooltip" data-placement="top" title="Column sorted by savings amount">Savings</span></th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <th scope="row">1</th>
                            <td class='checkingInfo'> 0 </td>
                            <td class='savingsInfo'> 0 </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <h2>Services: </h2>
            <h4>Online ATM:</h4>
            <li>Deposit money into checking
                <input type="text" id="checkingDeposit" value="0">
                <!-- <button type='button' id="checkingDepositButton">submit</button> -->
                <button type="button" id="checkingDepositButton" class="btn btn-m btn-primary disabled">Submit</button>
            </li>
            <li>Withdraw money from savings
                <input type="text" id="savingsDeposit" value="0">
                <button type="button" id="savingsDepositButton" class="btn btn-m btn-primary disabled">Submit</button>
            </li>

            <h4>Account Transfer:</h4>
            <li>Transfer money from checking into savings
                <input type="text" id="checkingTransfer" value="0">
                <button type="button" id="checkingTransferButton" class="btn btn-m btn-primary disabled">Submit</button>
            </li>
            <li>Transfer money from savings into checking
                <input type="text" id="savingsTransfer" value="0">
                <button type="button" id="savingsTransferButton" class="btn btn-m btn-primary disabled">Submit</button>
            </li>

            <h4>Loan Calculator:</h4>

            <div>
                Intersted in getting a loan?
                <br>
                See how much it will cost you monthly now!
            </div>

            <br>

            <div class="container table-bordered col-6">
                <div class="row">
                    <div class="col-sm">
                        Enter your desired loan amount:
                    </div>
                    <div class="col-sm">
                        How many years?
                    </div>
                    <div class="col-sm">
                        Yearly interest:
                    </div>
                </div>
                <div class="row">
                    <div class="col-sm">
                        <span>$</span><input type="text" id="principalField" value="0">
                    </div>
                    <div class="col-sm">
                        <select class="form-select" id="yearsField" aria-label="Default select example">
                            <option selected>Select how many years</option>
                            <option value="5">5</option>
                            <option value="25">25</option>
                            <option value="30">30</option>
                        </select>
                    </div>
                    <div class="col-sm">
                        <input type="range" class="form-range" min="0" max="80" id="interestRateSlider" onInput="$('#rangeval').html($(this).val()/10)">
                        <span id="rangeval">4</span><span>%</span>
                    </div>
                </div>
                <div class="row">
                    <div class="col-10 text-center">
                        <button type="button" id="loanCalculateButton" class="btn btn-lg btn-primary">Calculate!</button>
                    </div>
                </div>
                <div class="row">
                    <div class="col-10 text-center">Monthly Payments: <span id="monthlyPaymentField"></span></div>
                </div>
            </div>
        </div>
    </body>

    </html>

<?php
}
?>