using System;

namespace Microsoft.Office.Interop.Word
{
    internal class ApplicationEvents2_DocumentOpen
    {
        private Action<Document> wdEvents2_NewDocument;

        public ApplicationEvents2_DocumentOpen()
        {
        }

        public ApplicationEvents2_DocumentOpen(Action<Document> wdEvents2_NewDocument)
        {
            this.wdEvents2_NewDocument = wdEvents2_NewDocument;
        }
    }
}