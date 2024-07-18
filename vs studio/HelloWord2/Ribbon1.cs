using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Controls.Ribbon;
using System.Windows.Forms;
using Microsoft.Office.Interop.Word;
using Microsoft.Office.Tools.Ribbon;
using static System.Windows.Forms.VisualStyles.VisualStyleElement;

namespace HelloWord2
{
    public partial class Ribbon1
    {
        
        private void Ribbon1_Load(object sender, RibbonUIEventArgs e)
        {
           // Global.Ribbons.
        }

        private void Access_All_Ribbons_Globals()
        {
             Globals.Ribbons.Ribbon1.comboBox1.Text = "Hello World";
            var rb = Globals.Ribbons.ElementAt(0); 
        }

        private void Access_Ribbons_By_Explorer()
        {
           // ThisRibbonCollection ribbonCollection =  Globals.Ribbons[Globals.ThisAddIn.Application.ActiveExplorer()];
            //ribbonCollection.Ribbon1.comboBox1.Text = "Hello World2";
        }

        private void button1_Click(object sender, RibbonControlEventArgs e)
        {
            //  System.Windows.Forms.MessageBox.Show("Wheel3D word extension");

            Console.WriteLine("button clicked"); 
            MessageBox.Show("Wheeler3 client add in");   
            
        }
    }
}
