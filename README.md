<h1>NextTypes Project</h1><p>NextTypes is a standards based <strong>information storage, processing and transmission</strong> <a href="https://nexttypes.com/article/system?lang=en">system</a> that integrates the characteristics of other systems such as <a href="https://en.wikipedia.org/wiki/Database">databases</a>, <a href="https://en.wikipedia.org/wiki/Programming_language">programming languages</a>, <a href="https://en.wikipedia.org/wiki/Communication_protocol">communication protocols</a>, <a href="https://en.wikipedia.org/wiki/File_system">file systems</a>, <a href="https://en.wikipedia.org/wiki/Document_management_system">document managers</a>, <a href="https://en.wikipedia.org/wiki/Operating_system">operating systems</a>, <a href="https://en.wikipedia.org/wiki/Application_framework">frameworks</a>, <a href="https://en.wikipedia.org/wiki/File_format">file formats</a> and <a href="https://en.wikipedia.org/wiki/Computer_hardware">hardware</a> in a single tightly integrated system using a common <a href="https://nexttypes.com/article/system?lang=en#data-types">data types system</a>.</p>

<p>The <a href="https://nexttypes.com/software/nexttypes?lang=en">reference implementation</a> is programmed in <a href="https://en.wikipedia.org/wiki/Java_(programming_language)">Java</a> 11 and uses the <a href="https://en.wikipedia.org/wiki/PostgreSQL">PostgreSQL</a> 11 database manager. It is distributed under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0</a> license, in an easily installable <a href="https://nexttypes.com/software/nexttypes?lang=en">WAR file</a>. To test the software interface (in read-only mode), without having to install it, you can explore <a href="https://nexttypes.com/">the project website</a>, which is developed using said software. In the menu on the left, in the "Control panel" section and in the buttons on the top/right of the page, you will find the available actions.</p>

<h2 class="home">Storage</h2>

<p>NextTypes integrates the <a href="https://www.postgresql.org/docs/11/datatype.html">primitive PostgreSQL data types</a> such as numbers, text, binary, dates, <a href="https://en.wikipedia.org/wiki/JSON">JSON</a> or <a href="https://en.wikipedia.org/wiki/XML">XML</a>, although giving them different names to simplify nomenclature and facilitate integration with other systems. For example, instead of using "smallint", "integer" and "bigint" the root "int" followed by the number of bits (int16, int32 and int64) is used or instead of "character varying" or "varchar" it is used "string" as in some programming languages.</p>
  
<p>It also adds other types of data such as <a href="https://en.wikipedia.org/wiki/HTML">HTML</a> or <a href="https://en.wikipedia.org/wiki/URL">URL</a>, and using PostgreSQL ability to create <a href="https://www.postgresql.org/docs/11/rowtypes.html">composite data types</a> for columns, it includes data types for files, documents, images, audios and videos. The binary content of these fields can be scanned with the <a href=
"https://en.wikipedia.org/wiki/ClamAV">ClamAV</a> antivirus. Complex data types such as <a href="https://en.wikipedia.org/wiki/HTML">HTML</a> or images are made up of elements that can be accessed individually and can be applied restrictions to them such as <a href="https://en.wikipedia.org/wiki/Markup_language">tags</a> or <a href="https://en.wikipedia.org/wiki/File_format">formats</a> allowed.</p>

<img alt="Primitive types" src="https://nexttypes.com/image_link_language/eb41b8fe-19f1-41f3-83ac-496e45ed1b75/image" title="Primitive types"/>

<p>The storage system is primarily <a href="https://en.wikipedia.org/wiki/SQL">SQL</a> based but is a <a href="https://en.wikipedia.org/wiki/Relational_database">relational</a>/<a href="https://en.wikipedia.org/wiki/Network_model">network</a>/<a href="https://en.wikipedia.org/wiki/Object_database">objects</a>/<a href="https://en.wikipedia.org/wiki/Computer_file">files</a> hybrid. Each table is a data type and each row in a table is an object. Each row has a column with an <a href="https://en.wikipedia.org/wiki/Identifier">identifier</a> (id) that is the <a href="https://en.wikipedia.org/wiki/Primary_key">primary key</a> and several columns with <a href="https://en.wikipedia.org/wiki/Metadata">metadata</a> similar to those of the <a href="https://en.wikipedia.org/wiki/Computer_file">files</a>: creation date (cdate), update date (udate) and backup made (backup). The rest of the columns are the data fields of the object. The date of creation (cdate) and alteration (adate) of the data types is also stored. Dates use the <a href="https://en.wikipedia.org/wiki/Coordinated_Universal_Time">UTC</a> standard and include hours, minutes, seconds, and milliseconds. The <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> standard is used for its representation in text.</p>

<img alt="NextTypes types and objects structure" src="https://nexttypes.com/image_link_language/6b7e494b-5397-415f-ad8b-e64cd73dda16/image" title="NextTypes types and objects structure"/>

<p>The fields of the objects can be of a primitive type or point to other objects using the identifier as a <a href="https://en.wikipedia.org/wiki/Foreign_key">foreign key</a>, thus forming a <a href="https://en.wikipedia.org/wiki/Network_model">network database</a>. The identifier is a text string with a <a href="https://en.wikipedia.org/wiki/Database_index">unique index</a> and by default it receives a <a href="https://en.wikipedia.org/wiki/Universally_unique_identifier">UUID</a>. This simplification and uniformity in the <a href="https://en.wikipedia.org/wiki/Primary_key">primary</a> and <a href="https://en.wikipedia.org/wiki/Foreign_key">foreign keys</a> facilitates automation and integration with other systems.</p>

<p>The correspondence with the <a href="https://en.wikipedia.org/wiki/Entity%E2%80%93relationship_model">entity-relationship</a> model is similar to that of the <a href="https://en.wikipedia.org/wiki/Relational_model">relational model</a>. Each entity is an object, attributes are fields, and relationships and multiple attributes are made with fields that refer to other objects.</p>

<p>Object identifier and fields can be part of non-unique, unique, and <a href="https://en.wikipedia.org/wiki/Full-text_search">full-text search</a> indexes. The definition of data types and objects can be exported and imported to/from <a href="https://en.wikipedia.org/wiki/JSON">JSON</a>, allowing backup copies and transfer of information between systems.</p>

<pre>{
  "name" : "article_language",
  "cdate" : "2015-04-01T14:30:16Z",
  "adate" : "2018-09-26T14:59:35.53Z",
  "fields" : {
    "title" : {
      "type" : "string",
      "length" : 254,
      "not_null" : true
    },
    "language" : {
      "type" : "language",
      "length" : 100,
      "not_null" : true
    },
    "text" : {
      "type" : "html",
      "not_null" : true
    },
    "article" : {
      "type" : "article",
      "length" : 100,
      "not_null" : true
    }
  },
  "indexes" : {
    "al_ft_search_index" : {
      "mode" : "fulltext",
      "fields" : [ "title", "text" ]
    }
  },
  "actions" : { }
}</pre>

<p>The alteration and update dates allow the implementation of an <a href="https://en.wikipedia.org/wiki/Optimistic_concurrency_control">optimistic concurrency control</a> system. If when altering a type or updating an object the last known date of alteration or update is indicated, the system checks that this date is the same as the current date of the type or object to rule out that it has been modified by another user at a later date.</p>

<p>The "backup" metadata is used to create an incremental <a href="https://en.wikipedia.org/wiki/Backup">backup system</a> of updated objects with full copies each a specified number of incremental copies. Every time an object is updated the metadata "backup" is changed to "false" to indicate that there is no copy of that object and it should be copied in the next backup.</p>

<p>Through the use of <a href="https://en.wikipedia.org/wiki/PostgreSQL">PostgreSQL</a> features such as <a href="https://wiki.postgresql.org/wiki/Transactional_DDL_in_PostgreSQL:_A_Competitive_Analysis">transactional DDL</a> and deferrable constraints, the system allows the creation or modification of various data types and objects in the same transaction while the system is in use.</p>

<p>To complement the <a href="https://en.wikipedia.org/wiki/Full-text_search">full-text search</a> system, the extraction of metadata and text from <a href="https://en.wikipedia.org/wiki/PDF">PDF</a>, <a href="https://en.wikipedia.org/wiki/OpenDocument">OpenDocument</a> and <a href="https://en.wikipedia.org/wiki/Office_Open_XML">Office Open XML</a> documents, <a href="https://en.wikipedia.org/wiki/Microsoft_Office">Microsoft Office</a> binary formats and other formats is added using <a href="https://tika.apache.org/">Tika</a>.</p>

<h2 class="home">Processing</h2>

<p>NextTypes is a mainly <a href="https://en.wikipedia.org/wiki/Relational_database">relational</a> system with some <a href="https://en.wikipedia.org/wiki/Object_database">object-oriented</a> features that facilitate its use and automation from <a href="https://en.wikipedia.org/wiki/Programming_language">programming languages</a>. Unlike <a href="https://en.wikipedia.org/wiki/Object%E2%80%93relational_mapping">object-relational</a> systems, it does not use an object-class correspondence, instead it uses a series of generic classes and methods for all types and objects that allow performing the basic actions (<a href="https://en.wikipedia.org/wiki/Create,_read,_update_and_delete">CRUD</a>) of creation, reading, updating and deletion.</p>

<p>For complex actions such as queries that use several tables it provides a system with a higher level of abstraction than <a href="https://en.wikipedia.org/wiki/Java_Database_Connectivity">JDBC</a> that facilitates the use of <a href="https://en.wikipedia.org/wiki/SQL">SQL</a> and integrates with the data types of the storage. This system allows parameterized queries with the execution of a single method and uses table and column names as parameters, which offers protection against <a href="https://en.wikipedia.org/wiki/SQL_injection">SQL injection</a>. It also allows you to use an array as a parameter.</p>

<p>Basic actions can be intercepted similar to <a href="https://en.wikipedia.org/wiki/Database_trigger">SQL triggers</a> to modify types or objects or perform additional actions. In addition, new actions can be defined and made available to the system.</p>

<p>In the <a href="https://en.wikipedia.org/wiki/Java_(programming_language)">Java</a> implementation some primitive data types in storage, such as numbers, have a direct correspondence with classes in the <a href="https://en.wikipedia.org/wiki/Java_virtual_machine">Java Runtime Enviroment</a>. For other types such as <a href="https://en.wikipedia.org/wiki/HTML">HTML</a>, documents, images or videos, the system has classes that integrate with the storage types and allow the creation and modification of data.</p>

<h2 class="home">Transmission</h2>

<p>The main protocol used is <a href="https://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol">HTTP</a>, although adapters can be created for any other protocol. The system includes an <a href="https://en.wikipedia.org/wiki/Simple_Mail_Transfer_Protocol">SMTP</a> adapter that allows the reception of emails and their conversion into objects. On top of <a href="https://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol">HTTP</a>, a <a href="https://en.wikipedia.org/wiki/Representational_state_transfer">REST</a> interface is built with authentication through <a href="https://en.wikipedia.org/wiki/X.509">X.509</a> certificates and passwords (encrypted with <a href="https://en.wikipedia.org/wiki/Bcrypt">bcrypt</a>), <a href="https://en.wikipedia.org/wiki/Basic_access_authentication">HTTP Basic Auth</a>, protection against <a href="https://en.wikipedia.org/wiki/Denial-of-service_attack">DoS</a> attacks and management of the <a href="https://en.wikipedia.org/wiki/Robots_exclusion_standard">robots</a> file and <a href="https://en.wikipedia.org/wiki/Sitemaps">web sitemap</a>. This interface allows access to all types of data, objects, fields, and elements with one <a href="https://en.wikipedia.org/wiki/URL">URL</a>.</p>

<img alt="NextTypes URL" src="https://nexttypes.com/image_link_language/10f850da-7bb6-4a64-af3c-a26b4fa20c1b/image" title="NextTypes URL"/>

<p>The storage, processing and transmission systems form an <a href="https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller">MVC</a> system that allows access to the data through different views or formats: <a href="https://en.wikipedia.org/wiki/HTML">HTML</a>, <a href="https://en.wikipedia.org/wiki/WebDAV">WebDAV</a>, <a href="https://en.wikipedia.org/wiki/CalDAV">CalDAV</a>, <a href="https://en.wikipedia.org/wiki/JSON">JSON</a>, <a href="https://en.wikipedia.org/wiki/XML">XML</a>, <a href="https://en.wikipedia.org/wiki/RSS">RSS</a>, <a href="https://en.wikipedia.org/wiki/ICalendar">iCalendar</a> or other systems. Each of these views can be modified for all types of data or objects or some of them to adapt it to the user's needs. <a href="https://en.wikipedia.org/wiki/UTF-8">UTF-8</a> encoding is used throughout the system to allow the use of text in different languages.</p>

<img alt="Storage, processing and transmission of information" src="https://nexttypes.com/image_link_language/fa40831d-ce9e-4273-bbaa-62c252b425a9/image" title="Storage, processing and transmission of information"/>

<p>The <a href="https://en.wikipedia.org/wiki/HTML">HTML</a> view provides a multi-language <a href="https://en.wikipedia.org/wiki/Graphical_user_interface">graphical interface</a> that allows to perform all the actions of the storage/processing system. It is programmed in <a href="https://en.wikipedia.org/wiki/HTML5">HTML5</a>/<a href="https://en.wikipedia.org/wiki/CSS">CSS3</a> with <a href="https://en.wikipedia.org/wiki/Scalable_Vector_Graphics">SVG</a> resizable icons, <a href="https://en.wikipedia.org/wiki/Web_Content_Accessibility_Guidelines">WCAG</a> compliance, text editors with <a href="https://en.wikipedia.org/wiki/Syntax_highlighting">syntax highlighting</a> (<a href="https://codemirror.net/">Codemirror</a>) or <a href="https://en.wikipedia.org/wiki/WYSIWYG">WYSIWYG</a> (<a href="https://www.tiny.cloud/">TinyMCE</a>) and protection against <a href="https://en.wikipedia.org/wiki/Cross-site_request_forgery">CSRF</a> and <a href="https://en.wikipedia.org/wiki/Cross-site_scripting">XSS</a>. This view integrates the <a href="https://en.wikipedia.org/wiki/HTML5">HTML5</a> data types with those of NextTypes, adding the necessary components to the interface to complement the controls provided by <a href="https://en.wikipedia.org/wiki/HTML5">HTML5</a>. In the <a href="https://nexttypes.com/example?lang=en&amp;view=html&amp;form=insert">"example" data type</a> you can see the controls for all primitive data types and object references. You can adapt the <a href="https://en.wikipedia.org/wiki/HTML">HTML</a> view by modifying the <a href="https://en.wikipedia.org/wiki/Document_Object_Model">DOM</a>.</p>
  
<p>Each object has an identifying <a href="https://en.wikipedia.org/wiki/QR_code">QR code</a> with the name of the server, the type of data and the identifier of the object. This code can be used to label real objects or read it from a <a href="https://en.wikipedia.org/wiki/Mobile_app">mobile application</a>. There is also the possibility to export some data in <a href="https://en.wikipedia.org/wiki/JSON-LD">JSON-LD</a> format.</p>

<p>Other views allow remote access to data by any system. Through the use of <a href="https://en.wikipedia.org/wiki/JavaScript">Javascript</a> and <a href="https://en.wikipedia.org/wiki/JSON">JSON</a> or <a href="https://en.wikipedia.org/wiki/XML">XML</a> views it is possible to access all the data and carry out information processing in the browser. The <a href="https://en.wikipedia.org/wiki/WebDAV">WebDAV</a> view allows you to access data as a <a href="https://en.wikipedia.org/wiki/File_system">file system</a> in which the data types and objects are folders and the fields are files. This allows you to open and modify any field of an object with an external application. For example, you can edit a document field with a word processing application.</p>

<p>The <a href="https://en.wikipedia.org/wiki/CalDAV">CalDAV</a> view is a modification of the <a href="https://en.wikipedia.org/wiki/WebDAV">WebDAV</a> view that allows you to expose some objects as calendar events. Those events can also be accessed using the <a href="https://en.wikipedia.org/wiki/ICalendar">iCalendar</a> view. The <a href="https://en.wikipedia.org/wiki/RSS">RSS</a> view allows access to object listings with any client of that format.</p>
