////////////////////////////////////////////////////////////////////////////////////////////
//
// ToLL - function to compute Latitude and Longitude given UTM Northing and Easting in meters
//
//  Description:
//    This member function converts input north and east coordinates
//    to the corresponding Northing and Easting values relative to the defined
//    UTM zone.  Refer to the reference in this file's header.
//
//  Parameters:
//    north   - (i) Northing (meters)
//    east    - (i) Easting (meters)
//    utmZone - (i) UTM Zone of the North and East parameters
//    lat     - (o) Latitude in degrees 
//    lon     - (o) Longitude in degrees
//
function ToLL(north,east,utmZone)
{ 
  // This is the lambda knot value in the reference
  var LngOrigin = DegToRad(utmZone * 6 - 183)

  // The following set of class constants define characteristics of the
  // ellipsoid, as defined my the WGS84 datum.  These values need to be
  // changed if a different dataum is used.    

  var FalseNorth = 0.  // South or North?
  //if (lat < 0.) FalseNorth = 10000000.  // South or North?
  //else          FalseNorth = 0.   

  var Ecc = 0.081819190842622       // Eccentricity
  var EccSq = Ecc * Ecc
  var Ecc2Sq = EccSq / (1. - EccSq)
  var Ecc2 = Math.sqrt(Ecc2Sq)      // Secondary eccentricity
  var E1 = ( 1 - Math.sqrt(1-EccSq) ) / ( 1 + Math.sqrt(1-EccSq) )
  var E12 = E1 * E1
  var E13 = E12 * E1
  var E14 = E13 * E1

  var SemiMajor = 6378137.0         // Ellipsoidal semi-major axis (Meters)
  var FalseEast = 500000.0          // UTM East bias (Meters)
  var ScaleFactor = 0.9996          // Scale at natural origin

  // Calculate the Cassini projection parameters

  var M1 = (north - FalseNorth) / ScaleFactor
  var Mu1 = M1 / ( SemiMajor * (1 - EccSq/4.0 - 3.0*EccSq*EccSq/64.0 -
    5.0*EccSq*EccSq*EccSq/256.0) )

  var Phi1 = Mu1 + (3.0*E1/2.0 - 27.0*E13/32.0) * Math.sin(2.0*Mu1)
    + (21.0*E12/16.0 - 55.0*E14/32.0)           * Math.sin(4.0*Mu1)
    + (151.0*E13/96.0)                          * Math.sin(6.0*Mu1)
    + (1097.0*E14/512.0)                        * Math.sin(8.0*Mu1)

  var sin2phi1 = Math.sin(Phi1) * Math.sin(Phi1)
  var Rho1 = (SemiMajor * (1.0-EccSq) ) / Math.pow(1.0-EccSq*sin2phi1,1.5)
  var Nu1 = SemiMajor / Math.sqrt(1.0-EccSq*sin2phi1)

  // Compute parameters as defined in the POSC specification.  T, C and D

  var T1 = Math.tan(Phi1) * Math.tan(Phi1)
  var T12 = T1 * T1
  var C1 = Ecc2Sq * Math.cos(Phi1) * Math.cos(Phi1)
  var C12 = C1 * C1
  var D  = (east - FalseEast) / (ScaleFactor * Nu1)
  var D2 = D * D
  var D3 = D2 * D
  var D4 = D3 * D
  var D5 = D4 * D
  var D6 = D5 * D

  // Compute the Latitude and Longitude and convert to degrees
  var lat = Phi1 - Nu1*Math.tan(Phi1)/Rho1 *
    ( D2/2.0 - (5.0 + 3.0*T1 + 10.0*C1 - 4.0*C12 - 9.0*Ecc2Sq)*D4/24.0
     + (61.0 + 90.0*T1 + 298.0*C1 + 45.0*T12 - 252.0*Ecc2Sq - 3.0*C12)*D6/720.0 )

  lat = RadToDeg(lat)

  var lon = LngOrigin + 
    ( D - (1.0 + 2.0*T1 + C1)*D3/6.0
      + (5.0 - 2.0*C1 + 28.0*T1 - 3.0*C12 + 8.0*Ecc2Sq + 24.0*T12)*D5/120.0) / Math.cos(Phi1)

  lon = RadToDeg(lon)

  // Create a object to store the calculated Latitude and Longitude values
  var sendLatLon = new PC_LatLon(lat,lon)

  // Returns a PC_LatLon object
  return sendLatLon
}

////////////////////////////////////////////////////////////////////////////////////////////
//
//  RadToDeg - function that inputs a value in radians and returns a value in degrees
//
function RadToDeg(value)
{
  return ( value * 180.0 / Math.PI )
}

////////////////////////////////////////////////////////////////////////////////////////////
//
//  DegToRad - function that inputs a value in degrees and returns a value in radians
//
function DegToRad(value)
{
  return ( value / 180.0 * Math.PI )
}

////////////////////////////////////////////////////////////////////////////////////////////
//
// PC_LatLon - this psuedo class is used to store lat/lon values computed by the ToLL 
//  function.
//
function PC_LatLon(inLat,inLon)
{
  this.lat       = inLat     // Store Latitude in decimal degrees
  this.lon       = inLon     // Store Longitude in decimal degrees
}