CREATE TABLE test
(
    ID   UInt8,
    DESC String
) ENGINE MergeTree() ORDER BY ID;

INSERT INTO test
VALUES (1, 'foo');
INSERT INTO test
VALUES (2, 'bar');
