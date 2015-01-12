#ifndef VECTOR3_H
#define VECTOR3_H
//------------------------------------------------------------------------------
// Delta Robot v8 - Supports RUMBA 6-axis motor controller
// dan@marginallycelver.com 2014-01-07
//------------------------------------------------------------------------------
// Copyright at end of file.
// please see http://www.github.com/MarginallyClever/DeltaRobotv8 for more information.


#include "Arduino.h"


class Vector3 {
public:
  // these usions allow the Vector3 to be used as a color component
  float x;
  float y;
  float z;

public:
  Vector3() {}

  
  Vector3( float xx, float yy, float zz ) {
    x = xx;
    y = yy;
    z = zz;
  }


  Vector3( float v[ 3 ] ) {
    x = v[ 0 ];
    y = v[ 1 ];
    z = v[ 2 ];
  }

  
  ~Vector3() {};


  Vector3 &MakeZero() {
    x=0;
    y=0;
    z=0;

    return *this;
  }

  
  Vector3 &Set( float xx, float yy, float zz ) {
    x = xx;
    y = yy;
    z = zz;

    return *this;
  }


  Vector3 operator + () const { // Unary negation
    return Vector3(*this);
  }


  Vector3 operator - () const { // Unary negation
    return Vector3( -x, -y, -z );
  }


  Vector3 operator *= ( float v ) { // assigned multiply by a float
    x *= v;
    y *= v;
    z *= v;

    return *this;
  }


  Vector3 operator /= ( float t ) { // assigned division by a float
    float v;

    if( t == 0.0f )
      v = 0;
    else
      v = 1.0f / t;

    x *= v;
    y *= v;
    z *= v;

    return *this;
  }


  Vector3 operator -= ( const Vector3 &v ) { // assigned subtraction
    x -= v.x;
    y -= v.y;
    z -= v.z;

    return *this;
  }


  Vector3 operator += ( const Vector3 &v ) { // assigned addition
    x += v.x;
    y += v.y;
    z += v.z;

    return *this;
  }


  Vector3 operator *= ( const Vector3 &v ) { // assigned mult.
    x *= v.x;
    y *= v.y;
    z *= v.z;

    return *this;
  }


  Vector3 operator ^= ( const Vector3 &v ) { // assigned cross product
    float nx, ny, nz;
    
    nx = ( y * v.z - z * v.y );
    ny =-( x * v.z - z * v.x );
    nz = ( x * v.y - y * v.x );
    x = nx;
    y = ny;
    z = nz;

    return *this;
  }

/*
  bool operator == ( const Vector3 &v ) const {
    return ( abs( x - v.x ) < 0.01f &&
             abs( y - v.y ) < 0.01f &&
             abs( z - v.z ) < 0.01f );
  }*/

/*
  bool operator != ( const Vector3 &v ) const {
    return ( abs( x - v.x ) > 0.01f ||
             abs( y - v.y ) > 0.01f ||
             abs( z - v.z ) > 0.01f );
  }*/


  // METHODS
  float Length() const {
    return (float)sqrt( x*x+y*y+z*z );
  }


  float LengthSquared() const {
    return x*x+y*y+z*z;
  }


  void Normalize() {
    float len, iLen;

    len = Length();
    if( !len ) iLen = 0;
    else iLen = 1.0f / len;

    x *= iLen;
    y *= iLen;
    z *= iLen;
  }


  float NormalizeLength() {
    float len, iLen;

    len = Length();
    if( !len ) iLen = 0;
    else iLen = 1.0f / len;

    x *= iLen;
    y *= iLen;
    z *= iLen;

    return len;
  }


  void ClampMin( float min ) { // Clamp to minimum
    if( x < min ) x = min;
    if( y < min ) y = min;
    if( z < min ) z = min;
  }


  void ClampMax( float max ) { // Clamp to maximum
    if( x > max ) x = max;
    if( y > max ) y = max;
    if( z > max ) z = max;
  }


  void Clamp( float min, float max ) { // Clamp to range ]min,max[
    ClampMin( min );
    ClampMax( max );
  }


  // Interpolate between *this and v
  void Interpolate( const Vector3 &v, float a ) {
    float b( 1.0f - a );

    x = b * x + a * v.x;
    y = b * y + a * v.y;
    z = b * z + a * v.z;
  }


  float operator | ( const Vector3 &v ) const { // Dot product
    return x * v.x + y * v.y + z * v.z;
  }


  Vector3 operator / ( float t ) const { // vector / float
    if( t == 0.0f )
      return Vector3( 0, 0, 0 );

    float s( 1.0f / t );

    return Vector3( x * s, y * s, z * s );
  }


  Vector3 operator + ( const Vector3 &b ) const { // vector + vector
    return Vector3( x + b.x, y + b.y, z + b.z );
  }


  Vector3 operator - ( const Vector3 &b ) const { // vector - vector
    return Vector3( x - b.x, y - b.y, z - b.z );
  }


  Vector3 operator * ( const Vector3 &b ) const { // vector * vector
    return Vector3( x * b.x, y * b.y, z * b.z );
  }


  Vector3 operator ^ ( const Vector3 &b ) const { // cross(a,b)
    float nx, ny, nz;

    nx = y * b.z - z * b.y;
    ny = z * b.x - x * b.z;
    nz = x * b.y - y * b.x;

    return Vector3( nx, ny, nz );
  }


  Vector3 operator * ( float s ) const {
    return Vector3( x * s, y * s, z * s );
  }

/*
  void Rotate( Vector3 &axis, float angle ) {
    float sa = (float)sin( angle );
    float ca = (float)cos( angle );
    Vector3 axis2( axis );
    float m[9];

    axis2.Normalize();

    m[ 0 ] = ca + (1 - ca) * axis2.x * axis2.x;
    m[ 1 ] = (1 - ca) * axis2.x * axis2.y - sa * axis2.z;
    m[ 2 ] = (1 - ca) * axis2.z * axis2.x + sa * axis2.y;
    m[ 3 ] = (1 - ca) * axis2.x * axis2.y + sa * axis2.z;
    m[ 4 ] = ca + (1 - ca) * axis2.y * axis2.y;
    m[ 5 ] = (1 - ca) * axis2.y * axis2.z - sa * axis2.x;
    m[ 6 ] = (1 - ca) * axis2.z * axis2.x - sa * axis2.y;
    m[ 7 ] = (1 - ca) * axis2.y * axis2.z + sa * axis2.x;
    m[ 8 ] = ca + (1 - ca) * axis2.z * axis2.z;

    Vector3 src( *this );

    x = m[0] * src.x + m[1] * src.y + m[2] * src.z;
    y = m[3] * src.x + m[4] * src.y + m[5] * src.z;
    z = m[6] * src.x + m[7] * src.y + m[8] * src.z;
  }*/
};



/**
* This file is part of Delta Robot v8.
*
* Delta Robot v8 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Delta Robot v8 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Delta Robot v8. If not, see <http://www.gnu.org/licenses/>.
*/
#endif

