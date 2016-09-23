

#Welly Splosher API
<img src="https://github.com/castroneves/festivalcheck-ui/blob/master/images/green2.png" height="300px" />


##Description
Provides refined music festival data based on listening data from a Last.Fm or Spotify account. Lineup and Rumour data is care of efestivals.co.uk, and schedule data care of clashfinder.com

##Endpoints
### Lineup and Rumours
#### Last.Fm
Get artists linked with festival that match those in Last.Fm user's listening history

    /{festival}/{year}/{username}
###### Path Params    
<dl>
  <dt>festival</dt>
  <dd>The festival key dictated by efestivals, e.g. for Glastonbury, use glastonbury. For V Festival, prefix with 'vvv'</dd>

  <dt>year</dt>
  <dd>Year of festival data. 2015 if not specified</dd>

  <dt>username</dt>
  <dd>A valid Last.Fm username</dd>
</dl>



***

Get artists linked with festival that are recommended based on Last.Fm user's listening history

    /rec/{festival}/{year}/{username}
###### Path Params     
<dl>
  <dt>festival</dt>
  <dd>The festival key dictated by efestivals, e.g. for Glastonbury, use glastonbury. For V Festival, prefix with 'vvv'</dd>

  <dt>year</dt>
  <dd>Year of festival data. 2015 if not specified</dd>
  
  <dt>username</dt>
  <dd>A valid Last.Fm username</dd>
</dl> 

***

#### Spotify
Get artists linked with festival that are recommended based on Spotify user's owned playlists and saved tracks

    /spotify/{festival}/{year}/{code}/{redirectUrl}
###### Path Params 
<dl>
  <dt>festival</dt>
  <dd>The festival key dictated by efestivals, e.g. for Glastonbury, use glastonbury. For V Festival, prefix with 'vvv'</dd>

  <dt>code</dt>
  <dd>Spotify Authorization Code</dd>
  
  <dt>year</dt>
  <dd>Year of festival data. 2015 if not specified</dd>
  
  <dt>redirectUrl</dt>
  <dd>Redirect URL used in acquiring Authorization Code</dd>
</dl>


***
Get artists linked with festival that are recommended based on Spotify user's owned playlists and saved tracks

    /spotify/rec/{festival}/{year}/{code}/{redirectUrl}
###### Path Params 
<dl>
  <dt>festival</dt>
  <dd>The festival key dictated by efestivals, e.g. for Glastonbury, use glastonbury. For V Festival, prefix with 'vvv'</dd>

  <dt>year</dt>
  <dd>Year of festival data. 2015 if not specified</dd>

  <dt>code</dt>
  <dd>Spotify Authorization Code</dd>
  
  <dt>redirectUrl</dt>
  <dd>Redirect URL used in acquiring Authorization Code</dd>
</dl>


#### Sample Response

    [
        {
            "name": "Frank Turner",
            "day": "Saturday",
            "stage": "Other Stage",
            "status": "Confirmed",
            "scrobs": "359",
            "recrank": 32
        },
        {
            "name": "Nick Lowe, Paul Carrack & Andy Fairweather Low",
            "day": "Saturday",
            "stage": "Acoustic Tent",
            "status": "Confirmed",
            "scrobs": "39",
            "recrank": 176,
            "matchString": "Paul Carrack"
        }
    ]

### Personalised Schedule
#### Last.Fm

Generate personalised schedule based on Last.Fm listening history

    /s/{festival}/{username}
###### Path Params    
<dl>
  <dt>festival</dt>
  <dd>The festival key dictated by clashfinder, e.g. for Glastonbury, use g. For V Festival, prefix with 'vvv'</dd>

  <dt>username</dt>
  <dd>A valid Last.Fm username</dd>
</dl>

###### Query Params
<dl>
  <dt>year</dt>
  <dd>Year of festival data. 2015 if not specified</dd>
</dl>

***

Generate personalised schedule from recommended artists based on Last.Fm user's listening history

    /s/rec/{festival}/{username}
###### Path Params     
<dl>
  <dt>festival</dt>
  <dd>The festival key dictated by clashfinder, e.g. for Glastonbury, use g. For V Festival, prefix with 'vvv'</dd>

  <dt>username</dt>
  <dd>A valid Last.Fm username</dd>
</dl> 

###### Query Params
<dl>
  <dt>year</dt>
  <dd>Year of festival data. 2015 if not specified</dd>
</dl>

***

Generate hybrid personalised schedule from Last.Fm user's listening history and recommended artists

    /s/h/{strategy}/{festival}/{username}
###### Path Params     
<dl>
  <dt>strategy</dt>
  <dd>'listened' to prefer listened artists in schedule, 'recco' to prefer recommended, no other values valid</dd>
  
  <dt>festival</dt>
  <dd>The festival key dictated by clashfinder, e.g. for Glastonbury, use g. For V Festival, prefix with 'vvv'</dd>

  <dt>username</dt>
  <dd>A valid Last.Fm username</dd>
</dl> 

###### Query Params
<dl>
  <dt>year</dt>
  <dd>Year of festival data. 2015 if not specified</dd>
</dl>

#### Spotify

Generate personalised schedule based on Spotify user's owned playlists and saved tracks

    /s/spotify/{festival}/{authcode}/{redirectUrl}
###### Path Params    
<dl>
  <dt>festival</dt>
  <dd>The festival key dictated by clashfinder, e.g. for Glastonbury, use g. For V Festival, prefix with 'vvv'</dd>

  <dt>authcode</dt>
  <dd>Spotify Authorization Code</dd>
  
  <dt>redirectUrl</dt>
  <dd>Redirect URL used in acquiring Authorization Code</dd>
</dl>

###### Query Params
<dl>
  <dt>year</dt>
  <dd>Year of festival data. 2015 if not specified</dd>
</dl>

***

Generate personalised schedule from recommended artists based on Spotify user's owned playlists and saved tracks

    /s/spotify/rec/{festival}/{authcode}/{redirectUrl}
###### Path Params    
<dl>
  <dt>festival</dt>
  <dd>The festival key dictated by clashfinder, e.g. for Glastonbury, use g. For V Festival, prefix with 'vvv'</dd>

  <dt>authcode</dt>
  <dd>Spotify Authorization Code</dd>
  
  <dt>redirectUrl</dt>
  <dd>Redirect URL used in acquiring Authorization Code</dd>
</dl>

###### Query Params
<dl>
  <dt>year</dt>
  <dd>Year of festival data. 2015 if not specified</dd>
</dl>

***

Generate hybrid personalised schedule from Spotify user's owned playlists, saved tracks and recommended artists 

    /s/h/spotify/{strategy}/{festival}/{authcode}/{redirectUrl}
###### Path Params    
<dl>
  <dt>strategy</dt>
  <dd>'listened' to prefer listened artists in schedule, 'recco' to prefer recommended, no other values valid</dd>

  <dt>festival</dt>
  <dd>The festival key dictated by clashfinder, e.g. for Glastonbury, use g. For V Festival, prefix with 'vvv'</dd>

  <dt>authcode</dt>
  <dd>Spotify Authorization Code</dd>
  
  <dt>redirectUrl</dt>
  <dd>Redirect URL used in acquiring Authorization Code</dd>
</dl>

###### Query Params
<dl>
  <dt>year</dt>
  <dd>Year of festival data. 2015 if not specified</dd>
</dl>

***
#### Sample Response
    {
        "sched": {
            "Friday": [
                {
                    "name": "Bastille",
                    "stage": "Main Stage",
                    "day": "Friday",
                    "startTime": "18:10",
                    "endTime": "19:10",
                    "scrobs": 26,
                    "ttStart": "7.1667",
                    "ttDuration": "1.0000",
                    "reccorank": -1,
                    "start": 1440781800000,
                    "end": 1440785400000
                },
                {
                    "name": "Mumford & Sons",
                    "stage": "Main Stage",
                    "day": "Friday",
                    "startTime": "21:30",
                    "endTime": "23:30",
                    "scrobs": 231,
                    "ttStart": "10.5000",
                    "ttDuration": "2.0000",
                    "reccorank": -1,
                    "start": 1440793800000,
                    "end": 1440801000000
                }
            ],
            "Saturday": [
                {
                    "name": "Twin Atlantic",
                    "stage": "NME",
                    "day": "Saturday",
                    "startTime": "21:10",
                    "endTime": "22:00",
                    "scrobs": 72,
                    "ttStart": "10.1667",
                    "ttDuration": "0.8333",
                    "reccorank": -1,
                    "start": 1440879000000,
                    "end": 1440882000000
                }
            ],
            "Sunday": [
                {
                    "name": "Frank Turner (Solo)",
                    "stage": "Festival Republic",
                    "day": "Sunday",
                    "startTime": "21:35",
                    "endTime": "22:35",
                    "scrobs": 359,
                    "ttStart": "10.5833",
                    "ttDuration": "1.0000",
                    "reccorank": -1,
                    "matchString": "Frank Turner",
                    "start": 1440966900000,
                    "end": 1440970500000
                }
            ]
        },
        "clash": {
            "Friday": [
                {
                    "name": "Limp Bizkit",
                    "stage": "NME",
                    "day": "Friday",
                    "startTime": "21:10",
                    "endTime": "22:10",
                    "scrobs": 55,
                    "ttStart": "10.1667",
                    "ttDuration": "1.0000",
                    "reccorank": -1,
                    "start": 1440792600000,
                    "end": 1440796200000
                }
            ]
        }
    }


##Notes
