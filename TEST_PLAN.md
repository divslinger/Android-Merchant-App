These Android tests have been divided in sections: 
* Setup
* Detection
* Payment INPUT
* Payment REQUEST
* Settings
* Transactions,
* Navigation
* Compatibility

Some tests requires a fresh install without previous. 
This requires to both uninstall the app and to clear the app cache.
To clear the app cache, go to the app informations/setting:
* Go to OS settings => Storage => Select specific app
* OR an easier to access is to long-press the app icon and click on info
* Kill the app or 'Force stop' 
* Go to 'storage space & cache' (terms will differ on the phone)
* Click on 'clear data' & 'empty cache' AFTER ensuring that the app is not running
* Now, you can finally uninstall the app and all traces of previous install should be gone 

#### Setup
1. setting initial PIN-CODE  
   * TODO 
1. verify invoice is forbidden before setting payment target  
   * killing app before setting valid payment target prevent create invoice
   * TODO 
1. verify can not leave settings until payment target is set   
   * TODO

#### Detection    
1. verify language is set according to OS language
   * TODO 
1. verify default currency is set based on OS locale/country
   * TODO 

#### Payment INPUT    
1. verify that amount of 0 are forbidden 
   * TODO 
1. USD CURRENCY formatting & keyboard 
   * TODO 
1. JPY CURRENCY formatting & keyboard 
   * TODO 
1. EUR CURRENCY formatting & keyboard
   * TODO 
1. verify automatic return to payment input screen when app is paused 
   * TODO 
1. verify amount is reset after payment
   * TODO 
   
#### Payment REQUEST    
1. create invoice shows QR code & correct FIAT amount 
   * TODO 
1. verify CHECKMARK & SOUND after payment is sent 
   * TODO 
1. Automatic RESUME AFTER CRASH
   * TODO 
1. Automatic RECONNECT after internet disconnection
   * TODO 
1. verify invoice CANCELLATION doesn't crash
   * enter any amount on the Payment INPUT screen
   * Click the 'CHARGE' button to enter Payment REQUEST screen
   * Click the 'CANCEL' button button to exit Payment REQUEST screen
   * Wait 3 seconds and ensure that the app did not crash
   * verify amount has been properly reset to 0 on the Payment INPUT screen
1. verify auto-cancellation after TIMER EXPIRY
   * TODO 
1. verify that 2 payments using xPubKey ends up in 2 different addresses 
   * TODO 

#### Settings    
1. verify that access to settings screen is SECURED BY PIN-CODE
   * TODO 
1. verify PIN-CODE change
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
   * TODO verify decimal for EURO & USD and none for JPY
   * verify availability of "," for USD
   * verify formatting of "," for USD

#### Transactions    
1. Verify empty TX list
   * navigate to screen &  
1. Verify TX list is populated correctly 
   * TODO 
1. verify newly paid invoice is added to the list
   * TODO 

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
1. Android OS 5.0.x
   * TODO 
1. Android OS-6
   * TODO 
1. Android OS-7
   * TODO 
1. Android OS-8
   * TODO 
1. Android OS-9
   * TODO 
1. Android OS-10
   * TODO 
