$fn=70;


//load_all();
//make_all();
//make_all2();

load_all_dim();

module make_all2() {
    import("rotor/without_top.stl"); 
    import("rotor/top_modify.stl"); 
}

module make_all() {
    
   translate([-15,-15,-18.5])
import("rotor/curve_base.stl"); 
    
//rotate([-90,0,0])
//import("rotor/wheel_base.stl");
////adding encoder box
//for(i=[0:2]) 
//  translate([0, i*20, 0])
//    rotate([-90,0,0]){
//          color("red")
//     translate([ 0,0,-12])  import("rotor/rotor_head_full_gear.stl");
//        
//        color("gray")
//        import("rotor/box.stl"); 
//      
//      color("blue")
//      translate([0, 0.5,-20])
//      import("rotor/bar_con.stl"); 
//        
//      
//} 
  translate([-15,-15,-18.5])
 import("rotor/rotor_with_base.stl");
 
translate([0, -15, -16])
difference() 
{

import("rotor/top_big.stl");

translate([8, 30, 26])
cube([20, 100,40], center=true);
    
    translate([34,0,7])
    cube([50,20,30], center=true);
}




translate([ 26,-13,4])
rotate([0,0,10])
import("rotor/button_2_small.stl");

//end module
}


module load_all() {
    
    rotate([-90,0,0])
import("rotor/wheel_base.stl");
 
translate([-26,0,0])
rotate([-90,0,0])import("rotor/box.stl");

 
translate([30,-20,0])
rotate([-90,0,0])
import("rotor/bar_con.stl");


translate([-30, 20,0])
rotate([-90,0,0])
import("rotor/rotor_head_full_gear.stl");

translate([-40,50,-16])
import("rotor/curve_base.stl");



translate([40,40,0])
//rotate([0,0,30])
import("rotor/button_2_small.stl");

translate([ -30, 120, -20])
import("rotor/top_big.stl"); 
}


module load_all_dim() {
    
    rotate([-90,0,0])
import("rotor/wheel_base.stl");
 
translate([-28,0,0])
rotate([-90,0, 0])import("rotor/box.stl");

 
translate([35,-20,0])
rotate([-90,0,0])
import("rotor/bar_con.stl");


translate([-40, 40,0])
rotate([ 85,0, -20])
import("rotor/rotor_head_full_gear.stl");

translate([-40,50,-16])
import("rotor/curve_base.stl");
 
translate([40,40,0])
 rotate([0,0, -120])
import("rotor/button_2_small.stl");

translate([ -30, 120, -20])
import("rotor/top_big.stl"); 
}