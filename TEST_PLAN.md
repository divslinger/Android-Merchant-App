These Android tests have been divided in sections: 
* Setup
* Detection
* **TODO** Payment INPUT
* Payment REQUEST
* **TODO** Settings
* Transactions
* Navigation 
* Compatibility

Some tests requires a fresh install without data from the previous installation. 
This requires to both uninstall the app and to clear the app cache.
* Long-press app icon to invoke a contextual menu
* Click 'App info'
* Click 'Force stop' to kill the app 
* Click 'Storage'
* Click 'Clear data' AFTER ensuring that the app is not running otherwise it is useless.
* Click 'Uninstall' app
* Now, all data from previous installation are gone 

#### Setup
1. Setting initial PIN-CODE & Verify user can not leave settings until payment target is set
   * Force stop + Clear data + Uninstall app
   * Install & Launch app
   * Confirm that it opens on PIN-CODE screen to create passcode
   * Enter Pin code: 1111
   * Enter Pin code: 2222 to fail confirmation
   * Confirm that an error message appears similar to "PIN code must match"
   * Enter Pin code: 1111
   * Enter Pin code: 1111 to confirm
   * Confirm that the current screen is the Settings screen
   * Press the device BACK button
   * Confirm that an error message prevent from leaving the Settings screen and click OK
   * Press the toolbar top left BACK button
   * Confirm that an error message prevent from leaving the Settings screen and click OK
   * Click the device 'Overview' button representing a square: It opens a list of thumbnail images of apps.
   * Flick the app or Click 'X' button to kill it or 'Force stop' the app
   * Launch app again
   * Confirm that it opens on the Settings screen
    
#### Detection    
1. Verify language & currency are set based on OS language & country
   * Force stop + Clear data + Uninstall app
   * Ensure that the default OS & device language is ENGLISH  
   * Install & Launch app
   * Confirm that texts are in 'ENGLISH' language  
   * Enter Pin code: 1111
   * Enter Pin code: 1111 to confirm
   * Confirm that Local Currency is 'USD'
   * Put app in background 
   * Add 2nd language of your choice (here French is used)
     * Click device 'Settings' button (usually a gear icon)
     * Click 'System' 
     * Click 'Languages & Input' 
     * Click 'Languages'
     * Click '+ Add language'
     * Select 'Francais' then 'France'
     * Move 'Francais (France)' to the top
     * This will change the OS language to 'Francais' (French)
   * Force stop + Clear data + Uninstall app
   * Install & Launch app
   * Confirm that texts are in 'FRENCH' language  
   * Confirm that Local Currency is 'EUR'
   
#### Payment INPUT    
1. Verify that amount of 0 are forbidden 
   * Enter 0 USD on the Payment INPUT screen
   * Click the "Charge" button
   * Ensure that a red banner at the bottom says "Invalid amount"
1. USD CURRENCY formatting & keyboard 
   * TODO 
1. JPY CURRENCY formatting & keyboard 
   * TODO 
1. EUR CURRENCY formatting & keyboard
   * TODO 
1. Verify automatic return to Payment INPUT screen when app is paused 
   * TODO 
1. Verify amount is reset after payment
   * Enter any amount on the Payment INPUT screen
   * Click the "Charge" button to enter the Payment REQUEST screen
   * Grab a wallet of your choice on a separate device and pay the invoice by scanning the QR code
   * When the green checkmark screen appears, press "Done" to exit the screen
   * Ensure the amount on the Payment INPUT screen is 0

#### Payment REQUEST    
1. create invoice shows QR code & correct FIAT amount 
   * Enter 5.00 USD on the Payment INPUT screen
   * Click the "Charge" button to enter the Payment REQUEST screen
   * Verify the amount label at the top of the Payment REQUEST screen matches the amount you entered in Step 1. (5.00 USD)
1. Verify CHECKMARK & SOUND after payment is sent
   * Make sure your device's volume is at the MAX setting
   * Enter any amount on the Payment INPUT screen
   * Click the "Charge" button to enter the Payment REQUEST screen
   * Grab a wallet of your choice on a separate device and pay the invoice by scanning the QR code
   * Upon payment, the Bitcoin Cash Register device should now have a green screen with a white checkmark, and your device should make a noticeable ding sound.
1. Invoice generation continue after internet disconnection
   * Turn off internet connection on the merchant device.
   * Enter any amount on the Payment INPUT screen
   * Click the "Charge" button to enter the Payment REQUEST screen
   * Upon seeing the "Error during invoice generation" popup, do not press continue.
   * Swipe down on your Android device to bring the notification shade into view.
   * Turn on your internet connection
   * Wait for the internet connection to be established
   * Press continue on the "Error during invoice generation" popup
   * The invoice should now be properly generated.
1. Automatic RESUME AFTER CRASH
   * Enter 8 USD on the Payment INPUT screen
   * Click the "Charge" button to enter the Payment REQUEST screen
   * Kill/Force close the app
   * Launch the app again 
   * Confirm that the app opens on the Payment REQUEST screen with an amount of 8 USD
1. Automatic RECONNECT after internet disconnection
   * Enter any amount on the Payment INPUT screen
   * Click the "Charge" button to enter the Payment REQUEST screen
   * Confirm connection icon is green/Connected
   * Swipe down on your Android device to bring the notification shade into view.
   * Turn off your internet connection
   * Wait 5 seconds
   * Confirm connection icon is red/Disconnected
   * Repeat Step 4
   * Turn on your internet connection
   * Wait for the internet connection to be established
   * Confirm connection icon is green/Connected
   * Make payment by scanning the QR code that is displayed.
   * Ensure that the CheckMark screen is shown upon payment.
1. Verify invoice CANCELLATION doesn't crash
   * enter any amount on the Payment INPUT screen
   * Click the 'CHARGE' button to enter Payment REQUEST screen
   * Click the 'CANCEL' button button to exit Payment REQUEST screen
   * Wait 3 seconds and ensure that the app did not crash
   * Verify amount has been properly reset to 0 on the Payment INPUT screen
1. Verify auto-cancellation after TIMER EXPIRY
   * Enter any amount on the Payment INPUT screen
   * Click the "Charge" button to enter the Payment REQUEST screen
   * Wait for the timer below the QR code to reach 0:00
   * Make sure the Payment REQUEST screen closes itself when the timer reaches 0:00
1. Verify that 2 payments using xPubKey ends up in 2 different addresses 
   * Enter any amount on the Payment INPUT screen
   * Click the "Charge" button to enter the Payment REQUEST screen
   * Pay the invoice
   * Go to the Transactions screen
   * View the transaction on the block explorer
   * Make note of the address in the output belonging to you
   * Repeat Steps 1 through 6
   * Compare the addresses and ensure they do NOT match

#### Settings    
1. Verify that access to settings screen is SECURED BY PIN-CODE
   * Open the app
   * Slide the drawer open from the left
   * Click the "Settings" button
   * The PIN screen should open
   * Enter an INCORRECT PIN
   * A message should pop up notifying you of the incorrect PIN
   * Enter the CORRECT PIN
   * Ensure you are setting to the SETTINGS screen
1. Verify PIN-CODE change
   * TODO 
1. changing COMPANY/MERCHANT name
   * TODO 
1. payment target using API KEY
   * TODO 
1. payment target using PubKey/BCH format with "bitcoincash:" prefix 
   * TODO 
1. payment target using PubKey/BCH format without "bitcoincash:" prefix
   * TODO    
1. payment target using PubKey using legacy format
   * TODO    
1. payment target using P2SH
   * TODO 
1. payment target using xPubKey
   * test 2 address
1. Camera to scan address xPubKey
   * TODO 
1. Camera to scan address API KEY
   * TODO 
1. Camera to scan address PubKey
   * TODO 
1. changing CURRENCY
   * TODO Verify decimal for EURO & USD and none for JPY
   * Verify availability of "," for USD
   * Verify formatting of "," for USD

#### Transactions    
1. Verify empty TX list
   * Force stop + Clear data + Uninstall app
   * Install & launch app & do required setup
   * Go to Transactions screen
   * Ensure that the list is empty and display a message saying that there is no TX available
1. Verify newly paid invoice is added to the list
   * Go to Payment INPUT screen
   * Enter a specific amount
   * Click Charge to generate an invoice/QR code
   * Pay the invoice
   * Go to Transactions screen
   * Ensure that the  
1. Verify TX list is populated correctly 
   * **NOT READY** - This test requires the deployment of the new Bitcoin Pay Console
   * Force stop + Clear data + Uninstall app
   * Install & launchapp & do required setup using a payment target that has paid invoices in the past 
   * Go to Transactions screen
   * Pull down to refresh the TX list
   * Ensure that the past invoices are downloaded & shown  
1. Verify TX id can be copied to clipboard
   * Prerequisite: TX must be visible in the Transactions screen 
   * Go to Transactions screen
   * Click on "Copy Transaction"
   * Open a browser
   * Click on the address bar
   * Paste the clipboard to ensure that it contains the expected TX id
1. Verify TX address can be copied to clipboard   
   * Prerequisite: TX must be visible in the Transactions screen 
   * Go to Transactions screen
   * Click on "Copy address"
   * Open a browser
   * Click on the address bar
   * Paste the clipboard to ensure that it contains the expected address
1. Verify TX can be viewed in blockchain explorer webpage   
   * Prerequisite: TX must be visible in the Transactions screen 
   * Go to Transactions screen
   * Click on a TX and selet "View Transaction"
   * Confirm that it opens a browser and display info about the same TX

#### Navigation
1. BACK button behavior 
   * TODO 
1. ABOUT: navigate to screen
   * launch app & do required setup
   * Go to 'About' screen
   * Confirm that it displays app name "Bitcoin Cash Register", version & year
   * Click on device 'Home' button to put the app in background
   * Resume the paused app
   * Confirm that it opens on the Payment INPUT screen with an amount of 0
1. PRIVACY POLICY: navigate to screen
   * launch app & do required setup
   * Go to 'Privacy Policy' screen
   * Confirm that it displays the correct content or web page 
   * Click on device 'Home' button to put the app in background
   * Resume the paused app
   * Confirm that it opens on the Payment INPUT screen with an amount of 0
1. SERVICE TERMS: navigate to screen
   * **NOT READY** webpage url is NOT yet defined
   * launch app & do required setup
   * Go to 'Service Terms' screen
   * Confirm that it displays the 'Service Terms' web page 
   * Click on device 'Home' button to put the app in background
   * Resume the paused app
   * Confirm that it opens on the Payment INPUT screen with an amount of 0
1. TERMS OF USE: navigate to screen
   * launch app & do required setup
   * Go to 'Terms of use' screen
   * Confirm that it displays the 'Terms of use' web page 
   * Click on device 'Home' button to put the app in background
   * Resume the paused app
   * Confirm that it opens on the Payment INPUT screen with an amount of 0
1. TRANSACTIONS: navigate to screen 
   * launch app & do required setup
   * Go to 'Transactions' screen
   * Click on device 'Home' button to put the app in background
   * Resume the paused app
   * Confirm that it opens on the Payment INPUT screen with an amount of 0
1. SETTINGS: navigate to screen
   * launch app & do required setup for Pin code & Payment Target
   * Go to 'Settings' screen
   * Click on device 'Home' button to put the app in background
   * Resume the paused app
   * Confirm that it opens on the Payment INPUT screen with an amount of 0
1. Payment INPUT: navigate to screen
   * launch app
   * Go to 'Payment INPUT' screen
   * Enter amount: 99
   * Click on device 'Home' button to put the app in background
   * Resume the paused app
   * Confirm that it opens on the 'Payment INPUT' screen with an amount of 99
1. Payment REQUEST: navigate to screen
   * launch app
   * Go to 'Payment INPUT' screen
   * Enter amount: 10
   * Click charge to arrive on the 'Payment REQUEST' screen
   * Click on device 'Home' button to put the app in background
   * Resume the paused app
   * Confirm that it opens on the 'Payment REQUEST' screen with an amount of 10

#### Compatibility
1. Use an Android device with OS 10
   * Go to Payment INPUT screen
   * Enter a specific amount
   * Click Charge to generate an invoice/QR code
   * Pay the invoice
   * Confirm that the checkmark is shown after the payment is sent 
1. Use an Android device with OS 9.x
   * perform same test as 'Use an Android device with OS 10' 
1. Use an Android device with OS 8.x
   * perform same test as 'Use an Android device with OS 10' 
1. Use an Android device with OS 7.x
   * perform same test as 'Use an Android device with OS 10' 
1. Use an Android device with OS 6.x
   * perform same test as 'Use an Android device with OS 10' 
1. Use an Android device with OS 5.x
   * perform same test as 'Use an Android device with OS 10' 
