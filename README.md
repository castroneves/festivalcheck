

#Welly Splosher API
<img src="https://github.com/castroneves/festivalcheck-ui/blob/master/images/green2.png" height="300px" />


##Description
Provides refined music festival data based on listening data from a Last.Fm or Spotify account. Lineup and Rumour data is care of efestivals.co.uk, and schedule data care of clashfinder.com

##Endpoints
### Lineup and Rumours
#### Last.Fm
Get artists linked with festival that match those in Last.Fm user's listening history

    /{festival}/{username}
<dl>
  <dt>festival</dt>
  <dd>The festival key dictated by efestivals, e.g. for Glastonbury, use glastonbury. For V Festival, prefix with 'vvv'</dd>

  <dt>username</dt>
  <dd>A valid Last.Fm username</dd>
</dl>
***

Get artists linked with festival that are recommended based on Last.Fm user's listening history

    /rec/{festival}/{username}
<dl>
  <dt>festival</dt>
  <dd>The festival key dictated by efestivals, e.g. for Glastonbury, use glastonbury. For V Festival, prefix with 'vvv'</dd>

  <dt>username</dt>
  <dd>A valid Last.Fm username</dd>
</dl> 

#### Spotify
Get artists linked with festival that are recommended based on Spotify user's owned playlists and saved tracks

    /spotify/{festival}/{code}/{redirectUrl}
<dl>
  <dt>festival</dt>
  <dd>The festival key dictated by efestivals, e.g. for Glastonbury, use glastonbury. For V Festival, prefix with 'vvv'</dd>

  <dt>code</dt>
  <dd>Spotify Authorization Code</dd>
  
  <dt>redirectUrl</dt>
  <dd>Redirect URL used in acquiring Authorization Code</dd>
</dl>

***
Get artists linked with festival that are recommended based on Spotify user's owned playlists and saved tracks

    /spotify/rec/{festival}/{code}/{redirectUrl}
<dl>
  <dt>festival</dt>
  <dd>The festival key dictated by efestivals, e.g. for Glastonbury, use glastonbury. For V Festival, prefix with 'vvv'</dd>

  <dt>code</dt>
  <dd>Spotify Authorization Code</dd>
  
  <dt>redirectUrl</dt>
  <dd>Redirect URL used in acquiring Authorization Code</dd>
</dl>

##Notes