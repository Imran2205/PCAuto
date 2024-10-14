using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using Word = Microsoft.Office.Interop.Word;
using Office = Microsoft.Office.Core;
using Microsoft.Office.Tools.Word;

using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using Microsoft.Office.Interop.Word;
using System.Windows.Controls.Ribbon;
using System.Windows.Forms;
using Microsoft.Office.Tools.Ribbon;
using System.Linq.Expressions;
using System.Windows.Automation;
using UIAutomationBlockingCoreLib;
using System.Collections;
using System.Timers;
using System.Speech.Synthesis;
using System.Runtime.InteropServices;
using System.Drawing; 
using System.Windows.Forms;
using System.Windows.Forms.VisualStyles;


/*
 * Helps: https://docs.microsoft.com/en-us/dotnet/framework/ui-automation/get-supported-ui-automation-control-patterns
 * https://stackoverflow.com/questions/10105396/given-an-automation-element-how-do-i-simulate-a-single-left-click-on-it
 * https://docs.microsoft.com/en-us/dotnet/framework/ui-automation/invoke-a-control-using-ui-automation?redirectedfrom=MSDN
 */




namespace HelloWord2
{
 


    public partial class ThisAddIn
    {
        [DllImport("User32.dll")]
        static extern IntPtr GetDC(IntPtr hwnd);

        [DllImport("User32.dll")]
        static extern int ReleaseDC(IntPtr hwnd, IntPtr dc);


        [DllImport("user32.dll")]
        static extern bool InvalidateRect(IntPtr hWnd, IntPtr lpRect, bool bErase);
       

        Socket client;
        
        bool is_live = true;



        IList<AutomationElement> tabs;
        int tabs_i = 0;
        IList<AutomationElement> groups;
        IList<AutomationElement> groupelements;

        Boolean isopen = false;
        SpeechSynthesizer synthesizer;
        AutomationElement filetab;

        Dictionary<int, int> groupmemory = new Dictionary<int, int>();      //ribbon index, current group 

        Dictionary<String, int> elementTracker= new Dictionary<String, int>();      //ribbon_i+"_"+group_i

        private void ThisAddIn_Startup(object sender, System.EventArgs e)
        {
            connect_wheel3d();
            Thread thr = new Thread(new ThreadStart(read_socket));
            thr.Start();

       //     var nc=this.Application.ActiveDocument.Words.Count;

             
         //   IRibbonExtension sr = Globals.Ribbons.First();
            // sr.ExtensionBase.
            ThisRibbonCollection ribbonCollection = Globals.Ribbons;
            //ribbonCollection.First().ExtensionBase.

            //int nr=Globals.Ribbons.Count();

            //Application.CommandBars.ExecuteMso("Copy"); 

            //   TabMail


            Microsoft.Office.Interop.Word.ApplicationEvents3_Event wdEvents2 = (Microsoft.Office.Interop.Word.ApplicationEvents3_Event)this.Application;
            wdEvents2.NewDocument += new Word.ApplicationEvents3_NewDocumentEventHandler(wdEvents2_NewDocument);
            //wdEvents2.DocumentOpen += new Word.ApplicationEvents3_DocumentOpenEventHandler(wdEvents2_NewDocument);


            synthesizer = new SpeechSynthesizer();
            synthesizer.SetOutputToDefaultAudioDevice();
            // synthesizer.Speak("I am ready"); 


            // String txt= "<speak><prosody rate=\"slow\" pitch=\"-5st\">Microsoft VSTO Application for wheeler</prosody></speak>";
            //synthesizer.SpeakAsync(txt);

          //  speak_pitch("ok",  20);
            
        }

        public void speak_pitch(String txt, int pitch)
        {
            /*
            string str = "<speak version=\"1.0\"";
            str += " xmlns=\"http://www.w3.org/2001/10/synthesis\"";
            str += " xml:lang=\"en-US\">";
            str += "<prosody pitch=\""+pitch+"\">"+txt+"</prosody>";
            str += "</speak>";
            synthesizer.SpeakSsml(str);
            */

            String pc = "high";
            if(pitch<50)
            {
                pc = "low";
            }  
            string str = "<speak version=\"1.0\"";
            str += " xmlns=\"http://www.w3.org/2001/10/synthesis\"";
            str += " xml:lang=\"en-US\">";
            str+= "<voice name=\"en - US - Guy24kRUS\">";
            str += "<prosody pitch=\""+pitch+"\">" + txt + "</prosody>";
            str += "</voice></speak>";

            send_line(str);
            synthesizer.SpeakSsml(str);
        }

        System.Timers.Timer aTimer = new System.Timers.Timer(2000);

        void wdEvents2_NewDocument(Word.Document Doc)
        {
           // MessageBox.Show("New Document Fires.", "New Document",   MessageBoxButtons.OK, MessageBoxIcon.Exclamation);
            // tabs = get_tabs();

            
            // Hook up the Elapsed event for the timer. 
            aTimer.Elapsed += OnTimedEvent;
            aTimer.AutoReset = true;
            aTimer.Enabled = true; 


        }

        private void OnTimedEvent(Object source, ElapsedEventArgs e)
        {
            //MessageBox.Show("timer");
            ((System.Timers.Timer)source).Dispose();
            tabs = get_tabs();
            send_line("__doc_ready__");
        }

        private void ThisAddIn_DocumentOpen(Word.Document document)
        {
            // tabs = get_tabs();
            //MessageBox.Show("document open event");
        } 


        private void ThisAddIn_Shutdown(object sender, System.EventArgs e)
        {
            //System.Windows.Forms.MessageBox.Show("The document is closing.");
            send_line("_closing_");

            is_live = false;
            Console.WriteLine("Closing Hello Word Add in");
            try
            {
                // Release the socket.    
                Client.Shutdown(SocketShutdown.Both);
                Client.Close();
            }
            catch (Exception ex)
            {
            }
        }

        public void send_line(String txt)
        {
            try
            {
                //reply    
                byte[] msg = Encoding.ASCII.GetBytes(txt+"\n");
                int bytesSent = Client.Send(msg);
            }
            catch (Exception exx) { }
        }

        public void connect_wheel3d()
        {
            try
            {

                IPHostEntry host = Dns.GetHostEntry("localhost");
                IPAddress ipAddress = host.AddressList[0];
                IPEndPoint remoteEP = new IPEndPoint(ipAddress, 4455);

                // Create a TCP/IP  socket.    
                Client = new Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp);


                Client.Connect(remoteEP);
                Console.WriteLine("Socket connected to {0}",
                    Client.RemoteEndPoint.ToString());
            }
            catch (Exception ex)
            {
                Console.WriteLine("connection exception : {0}", ex.ToString());
            }
        }


        public void read_socket()
        {

            while (true)
            {
                System.Threading.Thread.Sleep(10);

                try
                {
                    byte[] bytes = new byte[1024];

                    // Receive the response from the remote device.    
                    int bytesRec = Client.Receive(bytes);
                    String data = Encoding.ASCII.GetString(bytes, 0, bytesRec);
                    on_msg(data);

                    is_live = true;
                }
                catch (Exception ex)
                {
                    is_live = false;
                    //break;
                }

            }
        }


        public IList<AutomationElement> get_tabs()
        {
            AutomationElement window = AutomationElement.FromHandle(new IntPtr(Globals.ThisAddIn.Application.ActiveWindow.Hwnd));
            AutomationElementCollection tabs = window.FindAll(
              System.Windows.Automation.TreeScope.Descendants,
              new PropertyCondition(AutomationElement.ControlTypeProperty, ControlType.TabItem));


            IList<AutomationElement> tabselement = new List<AutomationElement>();

            IEnumerator enumerator =tabs.GetEnumerator();
            String names = "";
            int i = 0;
            while (enumerator.MoveNext())
            {
                AutomationElement item = (AutomationElement)enumerator.Current;
                tabselement.Add(item);
                names += item.Current.Name + ",";
                if(names.Contains("Foxit"))
                {
                    break;
                }
                groupmemory[i] = group_i; 
            }
            //MessageBox.Show("names=" + names);

            isopen = true;

            AutomationElementCollection buttons = window.FindAll(
                  System.Windows.Automation.TreeScope.Descendants,
                  new PropertyCondition(AutomationElement.ControlTypeProperty, ControlType.Button));
            filetab = buttons.Cast<AutomationElement>().FirstOrDefault(x => x.Current.Name == "File Tab");

            return tabselement;
        }

        public void ui_auto2()
        {
            AutomationElement window = AutomationElement.FromHandle(new IntPtr(Globals.ThisAddIn.Application.ActiveWindow.Hwnd));
            AutomationElementCollection obs = window.FindAll(
              System.Windows.Automation.TreeScope.Descendants,
              new PropertyCondition(AutomationElement.ControlTypeProperty, ControlType.TabItem));

           

            try
            {
                AutomationElement ins = obs.Cast<AutomationElement>().FirstOrDefault(x => x.Current.Name == "Insert");
                MessageBox.Show("ins=" + ins);

                AutomationPattern[] ps=ins.GetSupportedPatterns();
                String pnames = "";
                foreach(AutomationPattern ap in ps)
                {
                    String nm=ap.ProgrammaticName;
                    pnames += nm + ",";
                }
                MessageBox.Show("pnames=" + pnames);

                //System.Windows.Point p = ins.GetClickablePoint(); 
                //String data=p.X + "," + p.Y + " ; ";
                //MessageBox.Show("point=" + data);


                SelectionItemPattern pattern;
                try
                {
                    pattern =ins.GetCurrentPattern(SelectionItemPattern.Pattern) as SelectionItemPattern;
                }
                catch (InvalidOperationException ex)
                {
                    Console.WriteLine(ex.Message);  // Most likely "Pattern not supported."
                    return;
                }
                pattern.Select();


            }
            catch (Exception ex)
            {
                MessageBox.Show("ex=" + ex.Message);
            }

        }

        public void ui_auto_tab_contents(String tabname)
        {
            tabs = get_tabs();
        }
       
        public void doSpeak()
        {
            //<prosody rate="slow" pitch="-2st">Can you hear me now?</prosody>
            synthesizer.SpeakAsync("Microsoft VSTO Application for wheeler");
        }
        public void ui_auto()
        {
            AutomationElement window = AutomationElement.FromHandle(new IntPtr(Globals.ThisAddIn.Application.ActiveWindow.Hwnd));
            AutomationElementCollection buttons = window.FindAll(
                  System.Windows.Automation.TreeScope.Descendants, 
                  new PropertyCondition(AutomationElement.ControlTypeProperty, ControlType.Button));
            AutomationElement file = buttons.Cast<AutomationElement>().FirstOrDefault(x => x.Current.Name == "File Tab");
            //InvokePattern ipClickLoadSettings = (InvokePattern)file.GetCurrentPattern(InvokePattern.Pattern);
            // ipClickLoadSettings.Invoke();


            AutomationElementCollection obs = window.FindAll(
               System.Windows.Automation.TreeScope.Descendants,
               new PropertyCondition(AutomationElement.ControlTypeProperty, ControlType.TabItem));


            //AutomationElement ins = obs.Cast<AutomationElement>().FirstOrDefault(x => x.Current.Name == "Insert");
            // InvokePattern b = (InvokePattern)ins.GetCurrentPattern(InvokePattern.Pattern);
            //b.Invoke();

            Console.WriteLine("__ui_auto__");


            IEnumerator enumerator = obs.GetEnumerator();
             


            InvokePattern ip=null;
            String data = "";
            while (enumerator.MoveNext())
            {
                AutomationElement item = (AutomationElement) enumerator.Current;
                //System.Windows.Point p = item.GetClickablePoint();
                string s = item.Current.Name;
                data +=s + " , ";
                //data += p.X + "," + p.Y + " ; ";


                if (s.Equals("Insert"))
                {  
                    try
                    {
                        InvokePattern b = (InvokePattern)item.GetCurrentPattern(InvokePattern.Pattern);
                    }
                    catch (Exception ex)
                    {
                        
                        var stList = ex.StackTrace.ToString().Split('\\');
                        Console.WriteLine("Exception occurred at " + stList[stList.Count() - 1]);

                        MessageBox.Show("Error=" +ex.Message+"\n"+ stList);
                    }
                    
                }

                //System.Windows.Point p = item.GetClickablePoint();
                //MessageBox.Show("Clickable point=" + p.X + " , " + p.Y);
            }
             MessageBox.Show("ui_auto data=" + data); 
           
        }

        public void exec_mso(String id)
        {
            MessageBox.Show("executing mso=" +id);

            try
            {
                Application.CommandBars.ExecuteMso(id);
                send_line("mso executed " + id);
            }
            catch (Exception er)
            {
                send_line("exception=" + er.Message + " for cmd=" + id);
            } 
        }

        int group_i = 0, element_i = -1;

        public Socket Client { get => client; set => client = value; }


        Boolean isfirstclick = true;
        Boolean third_wh_alt = true;
        Boolean third_wh_alt2 = true;
        Boolean third_wh_alt1 = true;
        public void on_msg(String msg)
        { 
            if (isopen == false)
            {
                tabs = get_tabs();
            }

            msg = msg.Trim('\n');
            msg = msg.Trim(' ');
            msg = msg.Substring(0, msg.Length - 2);


            if (msg.Trim().StartsWith("g"))
            {
                //from gui. no speed reducing.
                msg = msg.Substring(1);
            }
            else
            {


                if (msg.Trim().Equals("next") || msg.Trim().Equals("prev"))     //speed half //speed half
                {
                    if (third_wh_alt)
                    {
                        third_wh_alt = false;
                    }
                    else
                    {
                        third_wh_alt = true;
                        //return;
                    }

                }

                if (msg.Trim().Equals("next2") || msg.Trim().Equals("prev2"))     //speed half  //speed half
                {
                    if (third_wh_alt2)
                    {
                        third_wh_alt2 = false;
                    }
                    else
                    {
                        third_wh_alt2 = true;
                        return;
                    }

                }
                if (msg.Trim().Equals("next1") || msg.Trim().Equals("prev1"))          //First Wheel  //speed half
                {
                    if (third_wh_alt1)
                    {
                        third_wh_alt1 = false;
                    }
                    else
                    {
                        third_wh_alt1 = true;
                        return;
                    }

                }
            }


            if (msg.Trim().Equals("next"))          //Third Wheel 
            {
                tabs_i++;
                if (tabs_i >= tabs.Count)
                {
                    send_line("__NO_NEXT__");
                    tabs_i = tabs.Count - 1;
                }
                else
                {
                    SelectionItemPattern pattern;
                    try
                    {
                        synthesizer.SpeakAsyncCancelAll();
                        pattern = tabs[tabs_i].GetCurrentPattern(SelectionItemPattern.Pattern) as SelectionItemPattern;
                        synthesizer.SpeakAsync("" + tabs[tabs_i].Current.Name + " tab selected");
                        pattern.Select();
                        //synthesizer.Speak("Ribbon " + tabs[tabs_i].Current.Name);
                        listGroups();
                    }
                    catch (InvalidOperationException ex)
                    {
                        Console.WriteLine(ex.Message);  // Most likely "Pattern not supported."
                        return;
                    }
                }

                refocus(tabs[tabs_i]);

            }
            else if (msg.Trim().Equals("prev"))   //Third Wheel 
            {
                tabs_i--;
                /*
                if(tabs_i==-1)
                {
                    try
                    {
                        InvokePattern b = (InvokePattern)filetab.GetCurrentPattern(InvokePattern.Pattern);
                        b.Invoke();
                    }
                    catch (Exception ex)
                    { 
                    }

                }
                */
                if (tabs_i < 0)
                {
                    send_line("__NO_PREV__");
                    tabs_i = 0;
                }
                else
                {
                    SelectionItemPattern pattern;
                    try
                    {
                        synthesizer.SpeakAsyncCancelAll();
                        pattern = tabs[tabs_i].GetCurrentPattern(SelectionItemPattern.Pattern) as SelectionItemPattern;
                        synthesizer.SpeakAsync(tabs[tabs_i].Current.Name + " tab selected");
                        pattern.Select();
                        // synthesizer.Speak("Ribbon " + tabs[tabs_i].Current.Name);
                        listGroups();
                    }
                    catch (InvalidOperationException ex)
                    {
                        Console.WriteLine(ex.Message);  // Most likely "Pattern not supported."
                        return;
                    }

                }
                refocus(tabs[tabs_i]);

                //synthesizer.Speak(tabs[tabs_i].Current.Name);

            }
            else if (msg.Trim().Equals("next2"))      //Second Wheel 
            {
                if (groups == null)
                {
                    return;
                }
                if (groups.Count > 0)
                {
                    group_i++;
                    if (group_i >= groups.Count)
                    {
                        send_line("__NO_NEXT_G__");
                        group_i = group_i - 1;
                    }

                    groupmemory[tabs_i] = group_i;

                    //
                    synthesizer.SpeakAsyncCancelAll();

                    AutomationElement item = groups[group_i];
                    send_line("group=" + item.Current.Name);

                    //release_border();
                    //draw_border(item);
                    //refresh_rectanble(item);
                    refocus(item);

                    groupelements = WalkEnabledElements(item);
                    try
                    {
                        item.SetFocus();
                        //groupelements[0].SetFocus(); 
                    }
                    catch (Exception ex)
                    {
                        send_line("error=" + ex.Message);
                    }
                    synthesizer.SpeakAsync("group " + item.Current.Name);
                }
            }
            else if (msg.Trim().Equals("prev2"))                    //Second Wheel 
            {
                if (groups == null)
                {
                    return;
                }
                if (groups.Count > 0)
                {
                    group_i--;
                    if (group_i < 0)
                    {
                        send_line("__NO_PREV_G__");
                        group_i = 0;
                    }

                    synthesizer.SpeakAsyncCancelAll();
                    groupmemory[tabs_i] = group_i;

                    AutomationElement item = groups[group_i];
                    send_line("group " + item.Current.Name);

                    //release_border();
                    // draw_border(item); 
                    //refresh_rectanble(item);
                    refocus(item);


                    groupelements = WalkEnabledElements(item);
                    try
                    {
                        item.SetFocus();
                        // groupelements[0].SetFocus(); 
                    }
                    catch (Exception ex)
                    {
                        send_line("error=" + ex.Message);
                    }

                    synthesizer.SpeakAsync("group " + item.Current.Name);
                }

            }
            else if (msg.Trim().Equals("next1"))            //First Wheel
            {
                if (groupelements == null)
                {
                    return;
                }
                if (groupelements.Count > 0)
                {
                    element_i++;
                    if (element_i >= groupelements.Count)
                    {
                        send_line("__NO_NEXT_E__");
                        element_i = element_i - 1;
                    }

                    //
                    elementTracker[tabs_i + "_" + group_i] = element_i;  //storing ribbon_group to element.

                    synthesizer.SpeakAsyncCancelAll();
                    AutomationElement item = groups[group_i];

                    AutomationElement element = groupelements[element_i];
                    send_line("group element=" + element.Current.Name);


                    try
                    {
                        element.SetFocus();
                    }
                    catch (Exception ex)
                    {
                        // synthesizer.Speak(element.Current.Name);
                    }
                    String type = getElementType(element);
                    synthesizer.SpeakAsync(element.Current.Name + type);
                }
            }
            else if (msg.Trim().Equals("prev1"))            //First Wheel
            {
                if (groupelements == null)
                {
                    return;
                }
                if (groupelements.Count > 0)
                {

                    element_i--;
                    if (element_i < 0)
                    {
                        send_line("__NO_PREV_E__");
                        element_i = 0;
                    }
                    synthesizer.SpeakAsyncCancelAll();

                    elementTracker[tabs_i + "_" + group_i] = element_i;

                    AutomationElement item = groups[group_i];

                    AutomationElement element = groupelements[element_i];
                    send_line("group element=" + element.Current.Name);

                    try
                    {
                        element.SetFocus();
                    }
                    catch (Exception ex)
                    {
                        //synthesizer.Speak(element.Current.Name);
                    }

                    String type = getElementType(element);
                    synthesizer.SpeakAsync(element.Current.Name + type);

                }

            }
            else if (msg.Trim().Equals("click"))            //forward click button
            {
                if (isfirstclick)
                {
                    init_form();
                    isfirstclick = false;
                    return;
                }

                AutomationElement group = groups[group_i];
                AutomationElement element = groupelements[element_i];
                send_line("clicking on=" + element.Current.Name);
                execute_supported_pattern(element);
            }
            else if (msg.Trim().Equals("back"))                                           //test
            {
                SendKeys.SendWait("{ESC}");
            }
            else if (msg.Trim().Equals("br"))                                           //test
            {

                AutomationElement item = groups[group_i];
                //draw_border(item);
                // release_border();
                //InvalidateRect(IntPtr.Zero, IntPtr.Zero, true);

                draw_form(item);
            }
            else if (msg.Trim().Equals("sr"))                                       //Start Transparent overlay
            {
                //start overlay transparent form.

                init_form();
            }
            else if (msg.Trim().Equals("srr"))                            //drawing on transparent test.
            {
                x += 50;

                try
                {
                    //f.Refresh();
                    f.Invoke(new MethodInvoker(delegate ()
                    {
                        f.Refresh();
                    }));
                }
                catch (Exception ex)
                {
                    send_line("Error=" + ex.Message);
                }
            }
            else if (msg.Trim().Equals("pg"))
            {
                AutomationElement element = groupelements[element_i];
                String name = getProgramati_name(element);
                send_line("pg:" + name);

            }
            else if (msg.Trim().StartsWith("ttsp="))
            {
                synthesizer.SpeakAsyncCancelAll();
                try
                {
                    msg = msg.Substring(5);
                    String[] info = msg.Split(',');
                    int x = Int32.Parse(info[0]);
                    int y = Int32.Parse(info[1]);
                    synthesizer.Rate = -6 + x / 8;
                    synthesizer.Speak(x + "%");
                    synthesizer.Rate = -6 + y / 8;
                    synthesizer.SpeakAsync(y + "%");
                }
                catch (Exception ex)
                {
                    send_line("Error=" + ex.Message);
                }

            }
            else if (msg.Trim().StartsWith("tts="))
            {
                synthesizer.SpeakAsyncCancelAll();
                try
                {
                    msg = msg.Substring(4);
                    if (msg.Contains("#"))
                    {

                        String[] info = msg.Split('#');
                        String txt = info[0];
                        int pitch = Int32.Parse(info[1]);
                        speak_pitch(txt, pitch);

                        // synthesizer.Rate = pitch;
                        //synthesizer.Speak(txt);

                    }
                    else
                    {
                        send_line("to speak=" + msg);
                        synthesizer.SpeakAsync(msg);
                    }

                }
                catch (Exception ex)
                {
                    send_line("Error=" + ex.Message);
                }

            }
            else
            {
                // exec_mso(msg);
                send_line("echo:" + msg);
            } 
        }

        IntPtr desktopPtr;
        

        Boolean gfirst = true;
        Graphics g;

        private void draw_border(AutomationElement element)
        { 
            System.Windows.Rect br  = element.Current.BoundingRectangle;

            if (gfirst)
            {
                //desktopPtr = GetDC(IntPtr.Zero);
               // gfirst = false;
            }
            SolidBrush b = new SolidBrush(Color.Blue);
            //Pen pn = new Pen(b); 
            Pen pn = new Pen(Color.Blue, 3);

          
            InvalidateRect(IntPtr.Zero, IntPtr.Zero, true);
            Thread.Sleep(200);


            desktopPtr = GetDC(IntPtr.Zero);
            g = Graphics.FromHdc(desktopPtr);
             

            String brd = "" + (int)br.X + "," + (int)br.Y + "," + (int)br.Width + "," + (int)br.Height;
            send_line("br=" + brd);
           g.DrawRectangle(pn, new System.Drawing.Rectangle((int)br.X, (int)br.Y, (int)br.Width, (int)br.Height));
            

            /*
            using (Graphics g = Graphics.FromHwnd(IntPtr.Zero))
            {
                //g.DrawEllipse(Pens.Black, pt.X - 10, pt.Y - 10, 20, 20);
                g.DrawRectangle(pn, new System.Drawing.Rectangle((int)br.X, (int)br.Y, (int)br.Width, (int)br.Height));

            }
            */


        }

        private void refresh_rectanble(AutomationElement element)
        {

            System.Windows.Rect br = element.Current.BoundingRectangle;
            x = (int) br.X;
            y = (int)br.Y;
            xd = (int)br.Width;
            yd = (int)br.Height;
            if(f==null)
            {
                return;
            }

            f.Invoke(new MethodInvoker(delegate () {
                f.Refresh();
            }));
        }

        private void refocus(AutomationElement element)
        {
            if (f == null)
            {
                //init_form();
                return;
            }
            //send_line("brining front");
            refresh_rectanble(element);

            f.Invoke(new MethodInvoker(delegate () {
                //f.BringToFront(); 
                f.WindowState = FormWindowState.Minimized;
                f.Show();
                f.WindowState = FormWindowState.Normal;
            }));
        }

        int x = 100, y = 100, xd = 40, yd = 40;

        Form f=null; 
        private void init_form()
        {
            f = new Form();
            f.BackColor = Color.Lime;
            f.TransparencyKey = Color.Lime;
            f.FormBorderStyle = FormBorderStyle.None;
            f.Bounds = Screen.PrimaryScreen.Bounds;

            System.Drawing.Rectangle rc =Screen.PrimaryScreen.Bounds;
            x = rc.X;
            y = rc.Y;
            xd = rc.Width;
            yd = rc.Height;

            f.Paint += (sender, e) =>
            { 
                Pen selPen = new Pen(Color.Blue, 3);  
                e.Graphics.DrawRectangle(selPen, x, y, xd, yd);
            };

            //f.Bounds = new System.Drawing.Rectangle(0, 0, 1200, 200);
            //f.Show();
            //f.Hide(); ; 

            //g = f.CreateGraphics();
            Thread t1 = new Thread(StartMe);
            t1.Name = "Custom Thread";
            //t1.IsBackground = true;
            t1.Start();
        }

        private void StartMe()
        {
            f.ShowDialog();
        }

        private void dojob()
        {
            

        }

        private void draw_form(AutomationElement element )
        {

            send_line("drawing_form...");

            System.Windows.Rect br = element.Current.BoundingRectangle;

            if (g == null)
            {
                g = f.CreateGraphics();
                
            }
            
            // f.Bounds = new System.Drawing.Rectangle((int)br.X, (int)br.Y, (int)br.Width, (int)br.Height);
            //g.Clear(Color.Transparent);

            Pen selPen = new Pen(Color.Blue, 3);
            g.DrawRectangle(selPen, (int)br.X, (int)br.Y, (int)br.Width, (int)br.Height);
            //g.Dispose();

            //f.ShowDialog(); 

            try
            {
               // f.Refresh();
                f.Invalidate();
                f.Validate();
            }catch(Exception ex)
            {
                send_line("Error=" + ex.Message);
            }
            
        }

        private void StartMe2()
        {

            f.ShowDialog();


            g = f.CreateGraphics();
            // f.Bounds = new System.Drawing.Rectangle((int)br.X, (int)br.Y, (int)br.Width, (int)br.Height);
            //g.Clear(Color.Transparent);

            Pen selPen = new Pen(Color.Blue, 3);
            g.DrawRectangle(selPen, (int)br.X, (int)br.Y, (int)br.Width, (int)br.Height);
            g.Dispose();

            f.Validate();
        }


        System.Windows.Rect br;
        private void form_ind(AutomationElement element)
        {
            br = element.Current.BoundingRectangle;

            if (f!=null)
            {
                f.Dispose();
            }

            f = new Form();
            f.BackColor = Color.Lime;
            f.TransparencyKey = Color.Lime;
            f.FormBorderStyle = FormBorderStyle.None;
            f.Bounds = Screen.PrimaryScreen.Bounds; 

            Thread t1 = new Thread(StartMe2);
            t1.Name = "Custom Thread";
            //t1.IsBackground = true;
            t1.Start();

        }



        private void release_border()
        {
            try
            {
               if(g==null)
                {
                    return;
                }  
                // g.Dispose();
                //InvalidateRect(IntPtr.Zero, IntPtr.Zero, true);
               // Thread.Sleep(50);
                //ReleaseDC(IntPtr.Zero, desktopPtr);
            }
            catch(Exception ex) { }
        }


        private String getProgramati_name(AutomationElement element)
        {
            AutomationPattern[] patterns = element.GetSupportedPatterns();
            String names = "";
            for (int i = 0; i < patterns.Length; i++)
            {
                AutomationPattern ap = patterns[i];
                names += ap.ProgrammaticName + ",";
            }
            return names;

            //invoke=button
            //ExpandCollapsePatternIdentifiers   changecase=??
            //ValuePatternIdentifiers.Pattern,ExpandCollapsePatternIdentifiers.Pattern   fontsize
        }

        private String getElementType(AutomationElement element)
        {
            AutomationPattern[] patterns = element.GetSupportedPatterns();
            String names = "";
            for (int i = 0; i < patterns.Length; i++)
            {
                AutomationPattern ap = patterns[i];
                names += ap.ProgrammaticName + ",";
            }

            if (names.ToLower().Contains("toggle"))
            {
                return "button";

            } else if (names.ToLower().Contains("invoke"))
            {
                return "button";
            }

            return "";
        }


        private void execute_supported_pattern(AutomationElement element)
        {
            AutomationPattern[] patterns = element.GetSupportedPatterns();
            String names = "";
            for (int i = 0; i < patterns.Length; i++)
            {
                AutomationPattern ap = patterns[i];
                names += ap.ProgrammaticName + ",";  
            }

            if(names.ToLower().Contains("toggle"))
            {
                TogglePattern tp = element.GetCurrentPattern(TogglePatternIdentifiers.Pattern) as TogglePattern;
                tp.Toggle();

            }
            else if(names.ToLower().Contains("select"))
            {
                SelectionItemPattern pattern = element.GetCurrentPattern(SelectionItemPattern.Pattern) as SelectionItemPattern;
                pattern.Select();
            }
            else if (names.ToLower().Contains("invoke"))
            {
                InvokePattern invokePattern =element.GetCurrentPattern(InvokePattern.Pattern) as InvokePattern;
                invokePattern.Invoke();
            }
            else if (names.ToLower().Contains("expandcollapsepatternidentifiers"))
            {
                ExpandOrCollapseElement(element);
            }
            else
            {
                //not supported.
                send_line("__not supported=" + names + "__");
                SendKeys.SendWait("{ENTER}");
            }


            //ExpandCollapsePatternIdentifiers.Pattern

        }

        public void ExpandOrCollapseElement(AutomationElement element)
        {
            ExpandCollapsePattern pattern = element.GetCurrentPattern(ExpandCollapsePattern.Pattern) as ExpandCollapsePattern;

            if (pattern != null)
            {
                ExpandCollapseState currentState = pattern.Current.ExpandCollapseState;

                if (currentState == ExpandCollapseState.Collapsed)
                {
                    pattern.Expand();
                }
                else if (currentState == ExpandCollapseState.Expanded)
                {
                    pattern.Collapse();
                }
            }
        }

        public void InvokeAutomationElement(AutomationElement automationElement)
        {
            var invokePattern = automationElement.GetCurrentPattern(InvokePattern.Pattern) as InvokePattern;
            invokePattern.Invoke();
        }

        private void listGroups( ) {

            AutomationElement ce = AutomationElement.FromHandle(new IntPtr(Globals.ThisAddIn.Application.ActiveWindow.Hwnd));
            AutomationElementCollection comps = ce.FindAll( System.Windows.Automation.TreeScope.Descendants,
            new PropertyCondition(AutomationElement.ControlTypeProperty, ControlType.Group));

            IEnumerator enumerator = comps.GetEnumerator();
            String names = ce.Current.Name + ",";

            groups = new List<AutomationElement>(); 
            while (enumerator.MoveNext())
            {
                AutomationElement item = (AutomationElement)enumerator.Current; 
                groups.Add(item);
            }
            //group_i = 0;
            try
            {
                group_i = groupmemory[tabs_i];  //recall last.
            }catch(Exception ex) {
                send_line("error=" + ex.Message);
                group_i = 0;
            }
        }

        private void showElements(AutomationElement root)
        {
            IList<AutomationElement> es = WalkEnabledElements(root);

            String names = "";
            for (int i = 0; i < es.Count; i++)
            {
                AutomationElement ae = es[i];
                names += ae.Current.Name + ",";
            }
            MessageBox.Show("parent="+root.Current.Name+"\nelements: " + names);
        }


        private IList<AutomationElement> WalkEnabledElements(AutomationElement rootElement )
        {
            IList<AutomationElement> es = new List<AutomationElement>();


            Condition condition1 = new PropertyCondition(AutomationElement.IsControlElementProperty, true);
            Condition condition2 = new PropertyCondition(AutomationElement.IsEnabledProperty, true);
            TreeWalker walker = new TreeWalker(new AndCondition(condition1, condition2));
            AutomationElement elementNode = walker.GetFirstChild(rootElement);
            while (elementNode != null)
            {
                //TreeNode childTreeNode = treeNode.Nodes.Add(elementNode.Current.ControlType.LocalizedControlType);
                //WalkEnabledElements(elementNode, childTreeNode);
                es.Add(elementNode);

                elementNode = walker.GetNextSibling(elementNode);
            }

            //element_i = 0;

            int a=0;
            if (elementTracker.TryGetValue(tabs_i + "_" + group_i, out a))
            {
                element_i = a;
            }else
            {
                element_i = -1;
            }
             

            return es;
        }



        #region VSTO generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InternalStartup()
        {
            this.Startup += new System.EventHandler(ThisAddIn_Startup);
            this.Shutdown += new System.EventHandler(ThisAddIn_Shutdown);
            this.Application.DocumentOpen += new Word.ApplicationEvents4_DocumentOpenEventHandler(ThisAddIn_DocumentOpen); 
        }
        
        #endregion
    }
}
