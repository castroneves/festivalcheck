

#Welly Splosher API
<img src="https://github.com/castroneves/festivalcheck-ui/blob/master/images/green2.png" height="300px" />


##Description
Provides refined music festival data based on listening data from a Last.Fm or Spotify account. Lineup and Rumour data is care of efestivals.co.uk, and schedule data care of clashfinder.com

##Endpoints
### Lineup and Rumours
#### Last.Fm
Get artists linked with festival that match those in Last.Fm user's listening history

    /{festival}/{username}
###### Path Params    
<dl>
  <dt>festival</dt>
  <dd>The festival key dictated by efestivals, e.g. for Glastonbury, use glastonbury. For V Festival, prefix with 'vvv'</dd>

  <dt>username</dt>
  <dd>A valid Last.Fm username</dd>
</dl>

###### Query Params
<dl>
  <dt>year</dt>
  <dd>Year of festival data. 2015 if not specified</dd>
</dl>

***

Get artists linked with festival that are recommended based on Last.Fm user's listening history

    /rec/{festival}/{username}
###### Path Params     
<dl>
  <dt>festival</dt>
  <dd>The festival key dictated by efestivals, e.g. for Glastonbury, use glastonbury. For V Festival, prefix with 'vvv'</dd>

  <dt>username</dt>
  <dd>A valid Last.Fm username</dd>
</dl> 

###### Query Params
<dl>
  <dt>year</dt>
  <dd>Year of festival data. 2015 if not specified</dd>
</dl>

#### Spotify
Get artists linked with festival that are recommended based on Spotify user's owned playlists and saved tracks

    /spotify/{festival}/{code}/{redirectUrl}
###### Path Params 
<dl>
  <dt>festival</dt>
  <dd>The festival key dictated by efestivals, e.g. for Glastonbury, use glastonbury. For V Festival, prefix with 'vvv'</dd>

  <dt>code</dt>
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
Get artists linked with festival that are recommended based on Spotify user's owned playlists and saved tracks

    /spotify/rec/{festival}/{code}/{redirectUrl}
###### Path Params 
<dl>
  <dt>festival</dt>
  <dd>The festival key dictated by efestivals, e.g. for Glastonbury, use glastonbury. For V Festival, prefix with 'vvv'</dd>

  <dt>code</dt>
  <dd>Spotify Authorization Code</dd>
  
  <dt>redirectUrl</dt>
  <dd>Redirect URL used in acquiring Authorization Code</dd>
</dl>

###### Query Params
<dl>
  <dt>year</dt>
  <dd>Year of festival data. 2015 if not specified</dd>
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



##Notes
