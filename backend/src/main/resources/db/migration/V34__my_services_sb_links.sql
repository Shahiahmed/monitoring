ALTER TABLE my_services
    ADD COLUMN IF NOT EXISTS epir_id            VARCHAR(100),
    ADD COLUMN IF NOT EXISTS sb_xml_request     TEXT,
    ADD COLUMN IF NOT EXISTS sb_xml_response    TEXT,
    ADD COLUMN IF NOT EXISTS sb_xsd_file        TEXT,
    ADD COLUMN IF NOT EXISTS sb_wsdl_file       TEXT,
    ADD COLUMN IF NOT EXISTS sb_data_format     TEXT;
