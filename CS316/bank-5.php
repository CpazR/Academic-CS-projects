<?php
// run locally with `php -S localhost:8080 bank-5.php`

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
    addToChecking($_POST['checkingDeposit']);
} elseif (isset($_POST['checkingWithdraw']) && $_POST['checkingWithdraw']) {
    removeFromChecking($_POST['checkingWithdraw']);
} elseif (isset($_POST['savingsWithdraw']) && $_POST['savingsWithdraw']) {
    removeFromSavings($_POST['savingsWithdraw']);
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

$hasPost = isset($_POST['checkingDeposit']) || isset($_POST['checkingWithdraw']) || isset($_POST['savingsWithdraw']) || isset($_POST['checkingTransfer']) || isset($_POST['savingsTransfer']);
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

function addToChecking($checking)
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

function removeFromChecking($checking)
{
    global $isConnected, $mySqlConnection;

    $newCheckingAmount = 0;

    // validate input
    if ($checking > 0 && $isConnected) {
        // calculate new savings
        $newCheckingAmount = getColumnValue('checking', false) - $checking;
        // initialize statement
        $updateStatement = $mySqlConnection->prepare('UPDATE accounts set checking = :checking');
        // apply parameters to prepared statement
        $updateStatement->bindParam(':checking', $newCheckingAmount);
        // execute
        $updateStatement->execute();
    }
    $response = array('checkingWithdraw' => $newCheckingAmount);
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
    $response = array('savingsWithdraw' => $newSavingsAmount);
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
        <title>Nth Bank of HTML&#x2122;</title>
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css" integrity="sha384-JcKb8q3iqJ61gNV9KGb8thSsNjpSL0n8PARn9HuZOnIxN0hoP+VmmDGMN5t9UJ0Z" crossorigin="anonymous">
        <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
        <script src="https://cdn.datatables.net/1.10.22/js/jquery.dataTables.min.js"></script>
    </head>

    <!-- Initialize navbar -->
    <nav class="navbar sticky-top navbar-expand-md navbar-light" style='background-color: rgb(255,210,255)'>
        <span class="navbar-item">
            <svg preserveAspectRatio="xMidYMid meet" viewBox="0 0 200 420" width="6%" height="6%" xmlns="http://www.w3.org/2000/svg" xmlns:svg="http://www.w3.org/2000/svg">
                <!-- Created with SVG-edit - http://svg-edit.googlecode.com/ -->
                <g>
                    <title>Nth bank of HTML! It's all smoke and mirrors folks!</title>
                    <rect stroke="#000000" class="logo" id="svg_1" height="161.56835" width="142.69064" y="242.4892" x="30.34533" stroke-width="5" fill="#ff0000" />
                    <rect stroke="#000000" class="logo" id="svg_4" height="34.2446" width="161.09352" y="214.56115" x="21.84173" stroke-width="5" fill="#7f7f7f" />
                    <polygon stroke="#000000" class="logo" stroke-width="5" points="133.56402015686035,172.5719518661499 105.54372596740723,214.38869380950928 60.20598030090332,198.4161138534546 60.20598030090332,146.72777462005615 105.54372596740723,130.75519466400146 133.56402015686035,172.5719518661499 " strokeWidth="5" strokecolor="#000000" fill="#ffffff" edge="48.44699" orient="x" sides="5" shape="regularPoly" id="svg_5" cy="122" cx="308" />
                    <polygon class="logo" stroke-width="5" stroke="#000000" points="181.4248161315918,120.77698111534119 146.07509231567383,169.43173575401306 88.87799453735352,150.84726309776306 88.87799453735352,90.70669913291931 146.07509231567383,72.12221884727478 181.4248161315918,120.77698111534119 " strokeWidth="5" strokecolor="#000000" fill="#ffffff" edge="60.14058" orient="x" sides="5" shape="regularPoly" id="svg_10" cy="172" cx="358" />
                    <polygon class="logo" stroke-width="5" stroke="#000000" points="121.82204580307007,64.51798963546753 80.80417776107788,120.97426557540894 14.435849666595459,99.40988874435425 14.435849666595459,29.62609052658081 80.80417776107788,8.06171178817749 121.82204580307007,64.51798963546753 " strokeWidth="5" strokecolor="#000000" fill="#ffffff" edge="69.78379" orient="x" sides="5" shape="regularPoly" id="svg_12" cy="65" cx="270" />
                    <polygon strokeWidth="5" strokecolor="#000000" fill="#ffffff" edge="0" orient="x" sides="5" shape="regularPoly" id="svg_13" cy="144" cx="297" />
                    <polygon strokeWidth="5" strokecolor="none" fill="#ffffff" edge="0" orient="x" sides="5" shape="regularPoly" id="svg_14" cy="118" cx="314" />
                    <polygon strokeWidth="5" strokecolor="none" fill="#ffffff" edge="0" orient="x" sides="5" shape="regularPoly" id="svg_15" cy="121" cx="314" />
                    <polygon stroke-width="5" points="318.13420107780274,119 317.3504874080794,120.07868932583327 316.0824120530192,119.66666666666667 316.0824120530192,118.33333333333333 317.3504874080794,117.92131067416673 318.13420107780274,119 " strokeWidth="5" strokecolor="none" fill="#ffffff" edge="1.33333" orient="x" sides="5" shape="regularPoly" id="svg_16" cy="119" cx="317" />
                </g>
            </svg>
        </span>
        <span class="navbar-item">Current checking account: <span class='checkingInfo'></span> </span>
        <span class="navbar-item">Current savings account: <span class='savingsInfo'></span> </span>
    </nav>

    <!-- Body containing all specified sections of page -->

    <body>

        <!-- javascript for page -->
        <script>
            /** Variables for reading account values */

            var checkingAccountAmount = 0;
            var savingsAccountAmount = 0;

            const dbName = "localDB";
            const relation = 'transactions';
            var db;
            var dbData = [];
            const serial = 1;

            /** apply styling once page is loaded */
            function init() {
                // styling for specific elements
                $('body').css('padding-bottom', '200px');
                $('.navbar-item').css('padding-right', '25px');
                $('h1').css('display', 'table-cell');
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

                // indexDB setup
                initDB();
            }

            function initDB() {
                let request = window.indexedDB.open(dbName, serial);
                request.onerror = function(event) {
                    alert('Error initializing database: ' + request.error.message);
                };

                request.onsuccess = function(event) {
                    console.log('Database successfully loaded.');
                    db = event.target.result;
                    db.onerror = function(event) {
                        alert('Error: ' + event.target.error.message);
                    };
                    // initialize datatable once db is initialized and assigned
                    updateDatabaseData();
                    initDataTable();
                };

                request.onupgradeneeded = function(event) {
                    console.log('Updating database...');
                    db = this.result;
                    db.onerror = function(event) {
                        alert('Error: ' + event.target.error.message);
                    };

                    const table = db.createObjectStore(relation, {
                        keyPath: 'transactionNumber',
                        autoIncrement: true
                    });
                    table.createIndex('transactionType', 'transactionType', {
                        unique: false
                    });
                    table.createIndex('transactionAmount', 'transactionAmount', {
                        unique: false
                    });
                    table.createIndex('transactionSource', 'transactionSource', {
                        unique: false
                    });
                    table.createIndex('transactionDestination', 'transactionDestination', {
                        unique: false
                    });
                    table.createIndex('transactionDateTime', 'transactionDateTime', {
                        unique: false
                    });
                }
            }

            /** Add a new antry into the indexed database */
            function addEntry(entry) {
                const table = db.transaction([relation], "readwrite").objectStore(relation);
                const request = table.put(entry);
                request.onsuccess = function() {
                    console.log('successfully added entry');
                    updateDatabaseData();
                };
            };

            /** Get data for datatable and update datatable */
            function updateDatabaseData() {
                const transaction = db.transaction([relation], 'readonly');
                const table = transaction.objectStore(relation);
                dbData = [];
                // given a relation, iterate over all data in the table
                table.openCursor().onsuccess = function(event) {
                    const cursor = event.target.result;
                    if (cursor) {
                        dbData.push(cursor.value);
                        cursor.continue();
                    } else {
                        if (dbData.length > 0) {
                            // obtained valid data, insert into datatable
                            $('#transactionTable').dataTable().fnClearTable();
                            $('#transactionTable').dataTable().fnAddData(dbData);
                            console.log('Updated datatable with data from ' + relation);
                        }
                    }
                };
            };

            /** For debug user only, don't have button in webpage */
            function deleteDB() {
                if (db) {
                    db.close();
                    db = null;
                };
                window.indexedDB.deleteDatabase(dbName);
                console.log('Database deleted');
            };

            /** Setup data table to map to data */
            function initDataTable() {
                const table = $('#transactionTable').DataTable({
                    data: $.map(dbData, function(value, key) {
                        return value;
                    }),
                    columns: [{
                        'data': 'transactionNumber'
                    }, {
                        'data': 'transactionAmount'
                    }, {
                        'data': 'transactionType'
                    }, {
                        'data': 'transactionSource'
                    }, {
                        'data': 'transactionDestination'
                    }, {
                        'data': 'transactionDateTime'
                    }]
                });
                table.draw();
            }


            /** Returns true/false depending on if input value is positive or negative */
            function inputIsValid(_buttonId) {
                let buttonValue = parseFloat($("#" + _buttonId).val());
                return buttonValue > 0;
            }

            /** Returns true/false, depending on if value is valid */
            function validateDepositValue(_depositValue) {
                let isValid = (parseFloat(_depositValue).toFixed(2) > 0);
                return isValid;
            }

            /** Returns true/false, depending on if value is valid */
            function validateTransferValue(_transferValue, _accountValue) {
                let isValid = (parseFloat(_transferValue) < parseFloat(_accountValue));
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
            function refreshInputFields() {
                $('#checkingDeposit').val('0');
                $('#checkingDeposit').keyup();
                $('#checkingWithdraw').val('0');
                $('#checkingWithdraw').keyup();
                $('#savingsWithdraw').val('0');
                $('#savingsWithdraw').keyup();
                $('#checkingTransfer').val('0');
                $('#checkingTransfer').keyup();
                $('#savingsTransfer').val('0');
                $('#savingsTransfer').keyup();
            }

            /** Create and perform request for deposit/withdrawal */
            function updateDepositValue(depositField, type, source, destination) {
                let fieldId = '#' + depositField;
                let buttonFieldId = fieldId + 'Button';
                let depositValue = $(fieldId).val();
                let serializedData = {};

                if (validateDepositValue(depositValue)) {
                    $(buttonFieldId).prop('disabled', true);
                    serializedData[depositField] = depositValue;
                    let request = buildDepositRequest(serializedData);

                    request.done(function(response) {
                        if (depositField == 'checkingDeposit' || depositField == 'checkingWithdraw') {
                            updateChecking(response[depositField]);
                        } else {
                            updateSavings(response[depositField]);
                        }
                        $(buttonFieldId).prop('disabled', false);
                        refreshInputFields();
                        addEntry({
                            'transactionAmount': depositValue,
                            'transactionType': type,
                            'transactionSource': source,
                            'transactionDestination': destination,
                            'transactionDateTime': new Date().toLocaleString()
                        });
                    });

                    request.fail(function() {
                        alert('Request failed');
                    });
                } else {
                    alert("Input is not valid, please enter a positive number.")
                }
            }

            /** Create and perform request for transfer */
            function updateTransferValue(transferField, type, source, destination) {
                let fieldId = '#' + transferField + 'Transfer';
                let buttonFieldId = fieldId + 'Button';
                let transferValue = $(fieldId).val();
                let accountValue = $('.' + transferField + 'Info').html().trim();
                let serializedData = {};

                if (validateTransferValue(transferValue, accountValue)) {
                    $(buttonFieldId).prop('disabled', true);
                    serializedData[transferField + 'Transfer'] = transferValue;
                    let request = buildDepositRequest(serializedData);

                    request.done(function(response) {
                        updateChecking(response['checking']);
                        updateSavings(response['savings']);
                        $(buttonFieldId).prop('disabled', false);
                        refreshInputFields();
                        addEntry({
                            'transactionAmount': transferValue,
                            'transactionType': type,
                            'transactionSource': source,
                            'transactionDestination': destination,
                            'transactionDateTime': new Date().toLocaleString()
                        });
                    });
                } else {
                    alert("Input is not valid, please enter a positive number that is within your checkings and/or savings.");
                }
            }


            // JS functions to call out to database and get current values of fields
            function getCheckingAmount() {
                let requestParams = {
                    checkingInfo: true
                };
                let request = buildGetRequest(requestParams);

                request.done(function(response) {
                    checkingAccountAmount = response['checkingAmount'];
                    updateChecking(checkingAccountAmount);
                });
            }

            function getSavingsAmount() {
                let requestParams = {
                    savingsInfo: true
                };
                let request = buildGetRequest(requestParams);

                request.done(function(response) {
                    savingsAccountAmount = response['savingsAmount'];
                    updateSavings(savingsAccountAmount);
                });
            }

            function calculateInteretMonthlyPayment(_loanAmount, _yearsToPay, _yearlyInterest) {
                let monthlyRate = _yearlyInterest / 12;
                let c = Math.pow((1 + monthlyRate / 100), (12 * _yearsToPay));
                let monthlyPayment = _loanAmount * (monthlyRate / 100) * (c / (c - 1));

                return Math.round(monthlyPayment * 100) / 100;
            }

            $(document).ready(function() {
                /* Initialize page */
                init();

                /* Button click calls */
                $('#checkingDepositButton').click(function() {
                    updateDepositValue('checkingDeposit', 'deposit', 'external', 'checking');
                });
                $('#checkingWithdrawButton').click(function() {
                    updateDepositValue('checkingWithdraw', 'withdraw', 'checking', 'external');
                });
                $('#savingsWithdrawButton').click(function() {
                    updateDepositValue('savingsWithdraw', 'withdraw', 'savings', 'external');
                });
                $('#checkingTransferButton').click(function() {
                    updateTransferValue('checking', 'transfer', 'checking', 'savings');
                });
                $('#savingsTransferButton').click(function() {
                    updateTransferValue('savings', 'transfer', 'savings', 'checking');
                });
                $('#loanCalculateButton').click(function() {
                    // get values from inputs then call loan function
                    let principal = parseFloat($('#principalField').val());
                    let years = parseInt($('#yearsField').val());
                    let interestRate = parseFloat($('#rangeval').html());
                    if (!isNaN(principal) && !isNaN(years) && !isNaN(interestRate))
                        $('#monthlyPaymentField').html(calculateInteretMonthlyPayment(principal, years, interestRate));
                    else
                        alert('Verify loan inputs are numeric.');
                });

                /** EXTRA CREDIT MATERIAL */
                $('h1').click(function() {
                    alert("Just trying to get ahead in life. (Get it? Because you clicked on a header? ... THIS IS THE LAST TIME YOU'll NEED TO READ THIS PUN I SWEAR!!!)");
                });

                /** Validate fields and enable/disable buttons */
                $('#checkingDeposit').keyup(function() {
                    let valid = inputIsValid('checkingDeposit');
                    if (valid) {
                        $('#checkingDepositButton').removeClass('disabled');
                    } else {
                        $('#checkingDepositButton').addClass('disabled');
                    }
                });

                $('#checkingWithdraw').keyup(function() {
                    let valid = inputIsValid('checkingWithdraw');
                    if (valid) {
                        $('#checkingWithdrawButton').removeClass('disabled');
                    } else {
                        $('#checkingWithdrawButton').addClass('disabled');
                    }
                });

                $('#savingsWithdraw').keyup(function() {
                    let valid = inputIsValid('savingsWithdraw');
                    if (valid) {
                        $('#savingsWithdrawButton').removeClass('disabled');
                    } else {
                        $('#savingsWithdrawButton').addClass('disabled');
                    }
                });

                $('#checkingTransfer').keyup(function() {
                    let valid = inputIsValid('checkingTransfer');
                    if (valid) {
                        $('#checkingTransferButton').removeClass('disabled');
                    } else {
                        $('#checkingTransferButton').addClass('disabled');
                    }
                });

                $('#savingsTransfer').keyup(function() {
                    let valid = inputIsValid('savingsTransfer');
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
            <div>
                <span>
                    <svg preserveAspectRatio="xMidYMid meet" viewBox="0 0 200 420" width="8%" height="8%" xmlns="http://www.w3.org/2000/svg" xmlns:svg="http://www.w3.org/2000/svg">
                        <!-- Created with SVG-edit - http://svg-edit.googlecode.com/ -->
                        <g>
                            <title>Nth bank of HTML! It's all smoke and mirrors folks!</title>
                            <rect stroke="#000000" class="logo" id="svg_1" height="161.56835" width="142.69064" y="242.4892" x="30.34533" stroke-width="5" fill="#ff0000" />
                            <rect stroke="#000000" class="logo" id="svg_4" height="34.2446" width="161.09352" y="214.56115" x="21.84173" stroke-width="5" fill="#7f7f7f" />
                            <polygon stroke="#000000" class="logo" stroke-width="5" points="133.56402015686035,172.5719518661499 105.54372596740723,214.38869380950928 60.20598030090332,198.4161138534546 60.20598030090332,146.72777462005615 105.54372596740723,130.75519466400146 133.56402015686035,172.5719518661499 " strokeWidth="5" strokecolor="#000000" fill="#ffffff" edge="48.44699" orient="x" sides="5" shape="regularPoly" id="svg_5" cy="122" cx="308" />
                            <polygon class="logo" stroke-width="5" stroke="#000000" points="181.4248161315918,120.77698111534119 146.07509231567383,169.43173575401306 88.87799453735352,150.84726309776306 88.87799453735352,90.70669913291931 146.07509231567383,72.12221884727478 181.4248161315918,120.77698111534119 " strokeWidth="5" strokecolor="#000000" fill="#ffffff" edge="60.14058" orient="x" sides="5" shape="regularPoly" id="svg_10" cy="172" cx="358" />
                            <polygon class="logo" stroke-width="5" stroke="#000000" points="121.82204580307007,64.51798963546753 80.80417776107788,120.97426557540894 14.435849666595459,99.40988874435425 14.435849666595459,29.62609052658081 80.80417776107788,8.06171178817749 121.82204580307007,64.51798963546753 " strokeWidth="5" strokecolor="#000000" fill="#ffffff" edge="69.78379" orient="x" sides="5" shape="regularPoly" id="svg_12" cy="65" cx="270" />
                            <polygon strokeWidth="5" strokecolor="#000000" fill="#ffffff" edge="0" orient="x" sides="5" shape="regularPoly" id="svg_13" cy="144" cx="297" />
                            <polygon strokeWidth="5" strokecolor="none" fill="#ffffff" edge="0" orient="x" sides="5" shape="regularPoly" id="svg_14" cy="118" cx="314" />
                            <polygon strokeWidth="5" strokecolor="none" fill="#ffffff" edge="0" orient="x" sides="5" shape="regularPoly" id="svg_15" cy="121" cx="314" />
                            <polygon stroke-width="5" points="318.13420107780274,119 317.3504874080794,120.07868932583327 316.0824120530192,119.66666666666667 316.0824120530192,118.33333333333333 317.3504874080794,117.92131067416673 318.13420107780274,119 " strokeWidth="5" strokecolor="none" fill="#ffffff" edge="1.33333" orient="x" sides="5" shape="regularPoly" id="svg_16" cy="119" cx="317" />
                        </g>
                    </svg>
                </span>
                <span>
                    <h1>Welcome to the Nth bank of HTML!&#x2122;</h1>
                </span>
            </div>
            <p>
                Where this has likely been done before!
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
            <h6>Checking:</h6>
            <li>Deposit money into checking
                <input type="text" id="checkingDeposit" value="0">
                <button type="button" id="checkingDepositButton" class="btn btn-m btn-primary disabled">Submit</button>
            </li>
            <li>Withdraw money from checking
                <input type="text" id="checkingWithdraw" value="0">
                <button type="button" id="checkingWithdrawButton" class="btn btn-m btn-secondary disabled">Submit</button>
            </li>
            <h6>Savings:</h6>
            <li>Withdraw money from savings
                <input type="text" id="savingsWithdraw" value="0">
                <button type="button" id="savingsWithdrawButton" class="btn btn-m btn-secondary disabled">Submit</button>
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

            <h4>Transactions:</h4>

            <div>
                <table id="transactionTable" class="table table-striped table-bordered table-compact table-hover">
                    <thead>
                        <tr>
                            <th>Transaction #</th>
                            <th>Amount in $</th>
                            <th>Type</th>
                            <th>Source</th>
                            <th>Destination</th>
                            <th>Date-time of transaction</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>X</td>
                            <td>X</td>
                            <td>X</td>
                            <td>X</td>
                            <td>X</td>
                            <td>X</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </body>

    </html>

<?php
}
?>