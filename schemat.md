## use cases

### user interface

* managing beacons:
    * listing all visible beacons
    * listing all works with beacons, that determines this work
    * listing all unassigned beacons sorted by signal strength
    * allow add unassigned beacon to work
    * allow remove beacons assigned to work
* managing works: 
    * add/remove work
    * list all in-work data
    * try to calculate aggregated in-work information

### service

* listening for beacons from bluetooth manager
* saving information about visible works 
    * list beacons
    * check which from them determines work
    * persist information in-work
    
## implementation details

* views based on fragments:
    * BT-Fragment - displaying information about bluetooth status
    * Beacons - listing information about currently visible beacons
    * Works - listing actual created works
    * WorkDetails - listing full info about work, with registered timestamps
* fragments that uses realtime information about beacons binds to service
* storage by Parse.com ?
* in first version of application:
    * service triggers notification when BT isn't enabled
    * notification loads app with BT-Fragment loaded
    * enabling bluetooth intent after button pressed (any autoshowing)
    * listing only raw timestamps
    * information about in-work is binary, nothing more (workId,timestamp)