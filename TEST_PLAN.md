These Android tests have been divided in sections: 
* Setup
* Detection
* **TODO** Payment INPUT
* **TODO** Payment REQUEST
* **TODO** Settings
* Transactions
* XXX Navigation 
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
   * TODO 
1. USD CURRENCY formatting & keyboard 
   * TODO 
1. JPY CURRENCY formatting & keyboard 
   * TODO 
1. EUR CURRENCY formatting & keyboard
   * TODO 
1. Verify automatic return to payment input screen when app is paused 
   * TODO 
1. Verify amount is reset after payment
   * TODO 
   
#### Payment REQUEST    
1. create invoice shows QR code & correct FIAT amount 
   * TODO 
1. Verify CHECKMARK & SOUND after payment is sent 
   * TODO 
1. Automatic RESUME AFTER CRASH
   * TODO 
1. Automatic RECONNECT after internet disconnection
   * TODO 
1. Verify invoice CANCELLATION doesn't crash
   * enter any amount on the Payment INPUT screen
   * Click the 'CHARGE' button to enter Payment REQUEST screen
   * Click the 'CANCEL' button button to exit Payment REQUEST screen
   * Wait 3 seconds and ensure that the app did not crash
   * Verify amount has been properly reset to 0 on the Payment INPUT screen
1. Verify auto-cancellation after TIMER EXPIRY
   * TODO 
1. Verify that 2 payments using xPubKey ends up in 2 different addresses 
   * TODO 

#### Settings    
1. Verify that access to settings screen is SECURED BY PIN-CODE
   * TODO 
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
   * TODO 
1. PRIVACY POLICY: navigate to screen
   * TODO 
1. SERVICE TERMS: navigate to screen
   * TODO 
1. TERMS OF USE: navigate to screen
   * TODO 

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
