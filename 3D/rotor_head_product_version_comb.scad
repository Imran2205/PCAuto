$fn=70;
//house:7x2.5x4cm

//last edit: 5 Feb 20
//translate([10,10,10]) color("green")
//import("big_base2.stl")

base_l=100;

back_color="darkgray";
back_trans=0.6;

//wheel center theke 80cm. max.
 
//sofar(wh=true); 
//import("rotor/curve_base.stl"); 
//rotate([0,0,10])
//translate([40,-4,15])
 //button_small();

// translate([45,34,3]) 
//base_top_big_modify();
//base_top_big();
//big_bottom();
 //button();


//
//translate([-20,-31.5,9])
// cube([88,63,33]); //reference.

single_rotor();

 //encoder_box();
//wheel_base_cons();
//base();

//button_small();
//temporary design.
//translate([0,-100,0]) {
//     //design_tmp(); 
//    difference() {
//         cube([20,20,2], center=true);
//    difference(){
//    cube([20,20,2], center=true);
//    cylinder(r=11, h=3, center=true);
//    }
//}
//}

//back button?
//translate([0,-118,0]) {
//     //design_tmp(); 
//    difference() {
//         cube([10,10,2], center=true);
//    difference(){
//    cube([10,10,2], center=true);
//    cylinder(r=6, h=3, center=true);
//    }
//}
//}

module big_bottom() {
     difference() 
{
//translate([-1.5,-5,0]) cube([base_l, 67,2]);
    
    
translate([-1.5,32,0]) 
    difference()
    {
        curve_base();
        translate([8,-18,-2])
        cube([16,36,6]);
        
         translate([44,-15,-2])
        cube([22,30,6]);
    }
// sofar(wh=false);
  sofar(wh=true);
    
    c_hole();
}


}
module button_small() {
     
//    translate([ 0, 2,0.5])
//      cube([22,4,11+10], center=true);
    
    difference()
    {
    cube([20,4,22], center=true);
        translate([-5 ,0,6])
        cube([6.3, 5,6.3], center=true);
          translate([5,0,6])
        cube([6.3, 5,6.3], center=true);
         
         translate([-14,0,-10])
        cube([20, 5,20], center=true);
         
         translate([14,0,-10])
        cube([20, 5,20], center=true);
    }
    translate([0,4,-11.5])
    difference(){
    cube([12,12,3],center=true);
        translate([0,1.5,0])
        cube([3,2,5], center=true);
        
//        translate([0,-4.5,0])
//        cube([3,2,5], center=true);
    }
}

module base_top() {
    
   
    
      difference() {
    hull() {
    sphere(r=24);
//        cylinder(r=24, h=10);
    translate([30,0,03]) sphere(r=26);
    }
     translate([-20,0,-30]) cube([20,130,180], center=true);
    
     translate([0,0,-30]) cube([180,160,80], center=true);
    
    
     for(i=[0:1]) {
        translate([-5,-18+i*36,0])
    cylinder(r=1.5, h=20);
        translate([45,-16+i*32,0]) cylinder(r=1.5, h=40);
    }
    
    hull(){
        translate([-15,0,0])
         cylinder(r=24, h=5);
    translate([30,0,00]) sphere(r=26);
    }
}
    
//    difference()
//    {
//    color("blue") sphere(r=60, center=true);
//    translate([0,0,-20]) cube([130,130,80], center=true);
//    }
}


module base_top_big() {
    
   br=30;
   bd=36;
    
      difference() 
    {
    hull() {
    sphere(r=br+4);
//        cylinder(r=24, h=10);
    translate([bd,0,10]) sphere(r=br+2);
    }
     translate([-30,0,-30]) cube([20,130,180], center=true);
    
       translate([0,0,-8]) cube([80, 80,50], center=true);
    
     translate([0,0,-31]) cube([180,160,80], center=true);
    
    
      hull() {
    sphere(r=br-2+4);
//        cylinder(r=24, h=10);
    translate([bd,0,10]) sphere(r=br);
    }
    
 
////    
//    hull(){
//        translate([-15,0,0])
//         cylinder(r=24, h=5);
//    translate([30,0,00]) sphere(r=26);
//    }
}
//    
//    difference()
//    {
//    color("blue") sphere(r=60, center=true);
//    translate([0,0,-20]) cube([130,130,80], center=true);
//    }
}





module base_top_big_modify() {
    
   br=26;
   bd=30;
    
      difference() 
    {
    hull() {
    sphere(r=br+3);
//        cylinder(r=24, h=10);
    translate([bd,0,10]) sphere(r=br+1);
    }
     translate([-24,0,-30]) cube([20,130,180], center=true);
    
    rotate([0,6,0])
       translate([0,0,-12]) cube([80, 80,50], center=true);
    
     translate([0,0,-31]) cube([180,160,80], center=true);
    
    
      hull() {
    sphere(r=br-2+4);
//        cylinder(r=24, h=10);
    translate([bd,0,10]) sphere(r=br);
    }
    
 
////    
//    hull(){
//        translate([-15,0,0])
//         cylinder(r=24, h=5);
//    translate([30,0,00]) sphere(r=26);
//    }
}
//    
//    difference()
//    {
//    color("blue") sphere(r=60, center=true);
//    translate([0,0,-20]) cube([130,130,80], center=true);
//    }
}


module curve_base() {
    
  hull()
    {
        
        cube([3,30,3], center=true);
        translate([5,0,0])cube([3,50,3], center=true);
        translate([12,0,0])cube([3,60,3], center=true);
        
        translate([30,0,0])cube([3,62,3], center=true);
        
        translate([50,0,0])cube([3,60,3], center=true);
        translate([70,0,0])cube([3,53,3], center=true);
        translate([80,0,0])cube([3,40,3], center=true);
         translate([87,0,0])cube([3,38,3], center=true);
         translate([96,0,0])cube([3,24,3], center=true);
         translate([100,0,0])cube([3,10,3], center=true);
    }  
}

module c_hole() {
    color("red") cube([3,2,12], center=true);
}


module wheel3() {
    for(i=[0:2])
translate([12,2+i*20,20])
rotate([-90,0,0])
color("blue", 0.7) single_rotor();
}
module sofar(wh=true){
    
if (wh==true) {
    
//    translate([-55,0,0])
    wheel3();
}

translate([15,15,20.5])
rotate([-90,0,0])
 color("darkgray", 0.9) base(); 
 
//translate([-30,0,0])  //positioning
encoder_box();
 
//translate([35,0,0])  //positioning
wheel_base_cons();

// 
//rotate([0,0,5]) 
//translate([ 40,-3,16]) {
//    color(back_color, back_trans) button();
//    translate([0,4.5,-12]) c_hole();
////    translate([0,-4.5,-12]) c_hole();
//}
//
//rotate([0,0,5]) 
//translate([ 58,-2,16]){ 
//    color(back_color, back_trans) button(); 
//    translate([0,4.5,-12])  c_hole();
////    translate([0,-4.5,-12]) c_hole();
//}

}
module encoder_box() {
translate([15,15,20.5])
rotate([-90,0,0])
difference()
{
    union(){  
       for(i=[0:2]) {
           difference(){
            translate([0,0,i*20]) color("darkgray", 0.8) box(); 
         translate([-15,11,-1.5+i*20]) 
        cube([30,2,3]); //middle con cut 
           }  
       }  
    } 
         for(i=[0:2])
        translate([-15,11,-1.5+i*20]) 
        cube([30,2,3]); //middle con cut 
}    
    
}

module wheel_base_cons() {
    //the connector

translate([15,2.5,8])  //positioning
translate([-11,11,0]) 
for(i=[0:2]) { 
    translate([0, i*20, 0]) 
cube([22,3-0.5,2-0.8]); //middle con cut
}
}

module button(){
    
    difference(){
    cube([17,4,26], center=true);
        translate([0,0,4])
        cube([12, 5,12], center=true);
    }
    translate([0,4,-11.5])
    difference(){
    cube([17,12,3],center=true);
        translate([0,0.5,0])
        cube([3,2,5], center=true);
        
//        translate([0,-4.5,0])
//        cube([3,2,5], center=true);
    }
}

module nano_box(){
    
    difference(){
    cube([48,6,22], center=true);
        translate([0,3,0])
         cube([44,8,17.8], center=true);
    }
    
    
//    translate([0,5,-2])
//    cube([44,2,4]);
    
    
//    cube([3,18,2]);
//    
//    cube([44,3,2]);
//    translate([2,1.5,-3])
//        cylinder(d=1.2,h=5);
//    translate([2+40.7,1.5,-3])
//        cylinder(d=1.2,h=5);
//    
//     translate([2 ,1.5+15,-3])
//        cylinder(d=1.2,h=5);
    
}


module base(){
    difference()
    {
     union(){
    translate([0, 16, 18])
    cube([21, 3,60], center=true);   //long base
    
      
         
        for(i=[0,10,20]) 
      translate([0,0,12.5-i*2]){
    difference(){
    translate([10, 14.5, 4+20])   //left butterfly
        cube([6,3,7]);    
      translate([12.5, 14, 6+20])   cube([2,5,3]);    
     }
   difference(){
    translate([-16.5 , 14.5, 4+20])   //right butterfly
        cube([6,3,7]);    
      translate([-15, 14, 6+20])  cube([2,5,3]);    
     }
    }     
   
    
     for(i=[0:2])
    translate([0,0,20*i]) {
    translate([7.7, 9, -3.5])   //left side con
    cube([3,6.5,7]);
     translate([5, 9, -3.5+6.2])   //left side exp
    cube([5.5,6,2]);  
     translate([-5-5.5, 9, -3.5+6.2])   //right side exp
    cube([5.5,6,2]);  
    translate([-7.7-3, 9, -3.5])  //right side con
    cube([3,6.5,7]);
    }
 

     }
      
     
     { //two box hole conn.
     translate([-3, 16, -9])
    cube([3, 8,2], center=true); //space cut for wire.
     translate([3, 16, -9])
    cube([3, 8,2], center=true); //space cut for wire.
     }
     
     for(i=[0:2])
      translate([0, 16, 7+i*20])
        cube([10, 8,12], center=true); //space cut for wire.
     
         //cut for box and connector. (TODO: CHECK)
     clr=0.2;
    for(i=[0:2]) { 
    translate([-12, 11.25,  -1.5+i*20]) 
cube([26,2-0.8+clr,3-0.5+clr]); //middle con cut
}
     
 }
 
    for(i=[0:2])
        translate([-10,12,-5.3+i*20]) 
            cube([20,3,2]); //middle conn
    

    
 
//    translate([0,0,20.5])
//rotate([-90,0,0])
// wheel_base_cons();
 
}

module single_rotor() {
upper();
lower();
outer();


n2 = 77; //teeth 
 
mm_per_tooth = 1; 
thickness    = 11;
hole         = 10; 

 
translate([0,0, -2.5])
gear(mm_per_tooth,n2,thickness,hole,0,168);

outer_rim_d=24;   //will be 24
translate([0,0, 3])
difference(){
cylinder(d=outer_rim_d, h=10, center=true, $fn=100);
   cylinder(d=12, h=19, center=true, $fn=100);
     
}
}



module box(){
    //4, 13, 14
    difference()
    {
        translate([0,3,0])
 cube([15,16+7,6], center=true);
       union() {
        cube([12.6,13.6,8], center=true); //inner box
        translate([0,8,3])
       cube([12.6,24,6], center=true); //half cut
        }
        
        
//        translate([-10,13,-1.5]) {
//        cube([30,2,3]);
//        }
    }
    
    
//    translate([0,1,0]) 
//    difference()
//    {
//    translate([0,13.5,-4]) 
//    cube([15,5,8], center=true); //half cut
//        
//        translate([0,12,-4]) 
//    cube([12,5,12], center=true); //half cut
//        
//          
//        translate([-10,12.5,-6]) {
//        cube([20,2,3]);
//        }
//    }
    
}
module upper() {
    difference() {
    cylinder(r=5, h=5, center=true);
        cylinder(r=3.2, h=6, center=true, $fn=100);
    }

    translate([0,3,-0.5])
    cube([6,2,4], center=true);
}

module lower() { 
    translate([0,0, 4])
    difference() {
    cylinder(d=9, h=4, center=true);
        cylinder(d=7.5, h=6, center=true, $fn=100);
    }   
}

module outer(){
    difference() {
        translate([0,0,3])
    cylinder(d=12, h=11, center=true);
        cylinder(d=7.75, h=26, center=true, $fn=100);
    }
}


//////////////////////////////////////////////////////////////////////////////////////////////
// Public Domain Parametric Involute Spur Gear (and involute helical gear and involute rack)
// version 1.1
// by Leemon Baird, 2011, Leemon@Leemon.com
//http://www.thingiverse.com/thing:5505
//
// This file is public domain.  Use it for any purpose, including commercial
// applications.  Attribution would be nice, but is not required.  There is
// no warranty of any kind, including its correctness, usefulness, or safety.
// 
// This is parameterized involute spur (or helical) gear.  It is much simpler and less powerful than
// others on Thingiverse.  But it is public domain.  I implemented it from scratch from the 
// descriptions and equations on Wikipedia and the web, using Mathematica for calculations and testing,
// and I now release it into the public domain.
//
//		http://en.wikipedia.org/wiki/Involute_gear
//		http://en.wikipedia.org/wiki/Gear
//		http://en.wikipedia.org/wiki/List_of_gear_nomenclature
//		http://gtrebaol.free.fr/doc/catia/spur_gear.html
//		http://www.cs.cmu.edu/~rapidproto/mechanisms/chpt7.html
//
// The module gear() gives an involute spur gear, with reasonable defaults for all the parameters.
// Normally, you should just choose the first 4 parameters, and let the rest be default values.
// The module gear() gives a gear in the XY plane, centered on the origin, with one tooth centered on
// the positive Y axis.  The various functions below it take the same parameters, and return various
// measurements for the gear.  The most important is pitch_radius, which tells how far apart to space
// gears that are meshing, and adendum_radius, which gives the size of the region filled by the gear.
// A gear has a "pitch circle", which is an invisible circle that cuts through the middle of each
// tooth (though not the exact center). In order for two gears to mesh, their pitch circles should 
// just touch.  So the distance between their centers should be pitch_radius() for one, plus pitch_radius() 
// for the other, which gives the radii of their pitch circles.
//
// In order for two gears to mesh, they must have the same mm_per_tooth and pressure_angle parameters.  
// mm_per_tooth gives the number of millimeters of arc around the pitch circle covered by one tooth and one
// space between teeth.  The pitch angle controls how flat or bulged the sides of the teeth are.  Common
// values include 14.5 degrees and 20 degrees, and occasionally 25.  Though I've seen 28 recommended for
// plastic gears. Larger numbers bulge out more, giving stronger teeth, so 28 degrees is the default here.
//
// The ratio of number_of_teeth for two meshing gears gives how many times one will make a full 
// revolution when the the other makes one full revolution.  If the two numbers are coprime (i.e. 
// are not both divisible by the same number greater than 1), then every tooth on one gear
// will meet every tooth on the other, for more even wear.  So coprime numbers of teeth are good.
//
// The module rack() gives a rack, which is a bar with teeth.  A rack can mesh with any
// gear that has the same mm_per_tooth and pressure_angle.
//
// Some terminology: 
// The outline of a gear is a smooth circle (the "pitch circle") which has mountains and valleys
// added so it is toothed.  So there is an inner circle (the "root circle") that touches the 
// base of all the teeth, an outer circle that touches the tips of all the teeth,
// and the invisible pitch circle in between them.  There is also a "base circle", which can be smaller than
// all three of the others, which controls the shape of the teeth.  The side of each tooth lies on the path 
// that the end of a string would follow if it were wrapped tightly around the base circle, then slowly unwound.  
// That shape is an "involute", which gives this type of gear its name.
//
//////////////////////////////////////////////////////////////////////////////////////////////

//An involute spur gear, with reasonable defaults for all the parameters.
//Normally, you should just choose the first 4 parameters, and let the rest be default values.
//Meshing gears must match in mm_per_tooth, pressure_angle, and twist,
//and be separated by the sum of their pitch radii, which can be found with pitch_radius().
module gear (
	mm_per_tooth    = 3,    //this is the "circular pitch", the circumference of the pitch circle divided by the number of teeth
	number_of_teeth = 11,   //total number of teeth around the entire perimeter
	thickness       = 6,    //thickness of gear in mm
	hole_diameter   = 3,    //diameter of the hole in the center, in mm
	twist           = 0,    //teeth rotate this many degrees from bottom of gear to top.  360 makes the gear a screw with each thread going around once
	teeth_to_hide   = 0,    //number of teeth to delete to make this only a fraction of a circle
	pressure_angle  = 28,   //Controls how straight or bulged the tooth sides are. In degrees.
	clearance       = 0.0,  //gap between top of a tooth on one gear and bottom of valley on a meshing gear (in millimeters)
	backlash        = 0.0,   //gap between two meshing teeth, in the direction along the circumference of the pitch circle
    center = false,   // center gear by z axis
    $fn = 20   // number of fragments to draw hole cylinder
) {
	p  = mm_per_tooth * number_of_teeth / PI / 2;  //radius of pitch circle
	c  = p + mm_per_tooth / PI - clearance;        //radius of outer circle
	b  = p*cos(pressure_angle);                    //radius of base circle
	r  = p-(c-p)-clearance;                        //radius of root circle
	t  = mm_per_tooth/2-backlash/2;                //tooth thickness at pitch circle
	k  = -iang(b, p) - t/2/p/PI*180;             //angle to where involute meets base circle on each side of tooth
    difference() {
        for (i = [0:(number_of_teeth-teeth_to_hide-1 > 0 ? 1 : -1):number_of_teeth-teeth_to_hide-1] )
            rotate([0,0,i*360/number_of_teeth])
                linear_extrude(height = thickness, center = center, convexity = 10, twist = twist)
                    polygon(
                        points=[
                            [0, -hole_diameter/10],
                            polar(r, -181/number_of_teeth),
                            polar(r, r<b ? k : -180/number_of_teeth),
                            q7(0/5,r,b,c,k, 1),q7(1/5,r,b,c,k, 1),q7(2/5,r,b,c,k, 1),q7(3/5,r,b,c,k, 1),q7(4/5,r,b,c,k, 1),q7(5/5,r,b,c,k, 1),
                            q7(5/5,r,b,c,k,-1),q7(4/5,r,b,c,k,-1),q7(3/5,r,b,c,k,-1),q7(2/5,r,b,c,k,-1),q7(1/5,r,b,c,k,-1),q7(0/5,r,b,c,k,-1),
                            polar(r, r<b ? -k : 180/number_of_teeth),
                            polar(r, 181/number_of_teeth)
                        ],
                        paths=[[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]]
                    );
//        translate([0,0, (center ? 0 : -0.1)])
            cylinder(h=thickness+0.2, r=hole_diameter/2, center=center, $fn=$fn);
    }
};	
//these 4 functions are used by gear
function polar(r,theta)   = r*[sin(theta), cos(theta)];                            //convert polar to cartesian coordinates
function iang(r1,r2)      = sqrt((r2/r1)*(r2/r1) - 1)/PI*180 - acos(r1/r2); //unwind a string this many degrees to go from radius r1 to radius r2
function q7(f,r,b,r2,t,s) = q6(b,s,t,(1-f)*max(b,r)+f*r2);                         //radius a fraction f up the curved side of the tooth 
function q6(b,s,t,d)      = polar(d,s*(iang(b,d)+t));                              //point at radius d on the involute curve
 

//These 5 functions let the user find the derived dimensions of the gear.
//A gear fits within a circle of radius outer_radius, and two gears should have
//their centers separated by the sum of their pictch_radius.
function circular_pitch  (mm_per_tooth=3) = mm_per_tooth;                     //tooth density expressed as "circular pitch" in millimeters
function diametral_pitch (mm_per_tooth=3) = PI / mm_per_tooth;         //tooth density expressed as "diametral pitch" in teeth per millimeter
function module_value    (mm_per_tooth=3) = mm_per_tooth / PI;                //tooth density expressed as "module" or "modulus" in millimeters
function pitch_radius    (mm_per_tooth=3,number_of_teeth=11) = mm_per_tooth * number_of_teeth / PI / 2;
function outer_radius    (mm_per_tooth=3,number_of_teeth=11,clearance=0.1)    //The gear fits entirely within a cylinder of this radius.
	= mm_per_tooth*(1+number_of_teeth/2)/PI  - clearance;              

 
