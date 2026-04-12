from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path


ROOT = Path(r"e:\new\backend\category-analysis")
DATA_FILE = ROOT / "script" / "data.txt"
OUT_FILE = ROOT / "script" / "tmp" / "mock_category_tree_seed.sql"


@dataclass
class Node:
    code: str
    name: str
    level: int
    parent_code: str
    sku: int


def sql_text(value: str | None) -> str:
    if value is None:
        return "NULL"
    return "N'" + value.replace("'", "''") + "'"


def parse_data() -> dict[str, Node]:
    text = DATA_FILE.read_text(encoding="utf-8")
    nodes: dict[str, Node] = {}

    for raw in text.splitlines():
        line = raw.strip()
        if not line:
            continue

        code_candidates = re.findall(r"(\d{3,9})(?=\s*[^0-9\s])", line)
        if not code_candidates:
            continue

        # Pick the longest class-like code that is directly followed by class name text.
        # This avoids treating trailing SKU numbers as class code.
        code = sorted(code_candidates, key=len)[-1]
        if len(code) not in (3, 5, 7, 9):
            continue

        if len(code) == 3:
            level = 1
            parent = "0"
        elif len(code) == 5:
            level = 2
            parent = code[:3]
        elif len(code) == 7:
            level = 3
            parent = code[:5]
        else:
            level = 4
            parent = code[:7]

        tail = line.split(code, 1)[1].strip() if code in line else ""
        if "SKU" in tail:
            tail = tail.split("SKU", 1)[0]
        name = tail.strip(" ：:，,-")
        if not name:
            name = f"CLASS-{code}"

        sku_match = re.search(r"SKU[^0-9]*(\d+)?", line)
        sku = int(sku_match.group(1)) if sku_match and sku_match.group(1) else 0

        nodes[code] = Node(code=code, name=name, level=level, parent_code=parent, sku=sku)

    # Fill missing ancestors if any
    changed = True
    while changed:
        changed = False
        existing = list(nodes.values())
        for n in existing:
            if n.level == 1:
                continue
            parent = n.parent_code
            if parent in nodes:
                continue

            if len(parent) == 3:
                p_level, p_parent = 1, "0"
            elif len(parent) == 5:
                p_level, p_parent = 2, parent[:3]
            elif len(parent) == 7:
                p_level, p_parent = 3, parent[:5]
            else:
                continue

            nodes[parent] = Node(
                code=parent,
                name=f"CLASS-{parent}",
                level=p_level,
                parent_code=p_parent,
                sku=0,
            )
            changed = True

    return nodes


def build_rows(nodes: dict[str, Node]) -> tuple[list[str], list[str], list[str]]:
    sorted_nodes = sorted(nodes.values(), key=lambda x: (x.level, x.code))

    base_class_rows: list[str] = []
    class_sku_rows: list[str] = []
    leaf_seed_rows: list[str] = []

    role_map = {
        "001": "1",
        "002": "4",
        "003": "3",
        "004": "2",
    }

    for n in sorted_nodes:
        one = two = three = four = five = None
        one_name = two_name = three_name = four_name = five_name = None

        one_code = n.code[:3]
        one_node = nodes.get(one_code)
        if one_node:
            one, one_name = one_node.code, one_node.name

        if n.level >= 2:
            two_code = n.code[:5]
            two_node = nodes.get(two_code)
            if two_node:
                two, two_name = two_node.code, two_node.name
        if n.level >= 3:
            three_code = n.code[:7]
            three_node = nodes.get(three_code)
            if three_node:
                three, three_name = three_node.code, three_node.name
        if n.level >= 4:
            four_code = n.code[:9]
            four_node = nodes.get(four_code)
            if four_node:
                four, four_name = four_node.code, four_node.name

        base_class_rows.append(
            "("
            + ",".join(
                [
                    sql_text(one),
                    sql_text(one_name),
                    sql_text(two),
                    sql_text(two_name),
                    sql_text(three),
                    sql_text(three_name),
                    sql_text(four),
                    sql_text(four_name),
                    sql_text(five),
                    sql_text(five_name),
                ]
            )
            + ")"
        )

        role = role_map.get(one_code, "3")
        class_sku_rows.append(
            "("
            + ",".join(
                [
                    "N'S001'",
                    "N'STORE-A'",
                    "N'ORG01'",
                    "N'AREA-1'",
                    "N'1'",
                    "N'FORMAT-1'",
                    "N'1'",
                    "N'CIRCLE-1'",
                    "N'1'",
                    "N'GROUP-1'",
                    sql_text(n.code),
                    sql_text(n.name),
                    f"'{n.sku}'",
                    sql_text(role),
                ]
            )
            + ")"
        )

        has_child = any(x.parent_code == n.code for x in sorted_nodes)
        if not has_child:
            # Use a scaled count to avoid overly large insert size.
            gen_count = min(max(1, round(n.sku * 0.02)), 80)
            leaf_seed_rows.append(
                "("
                + ",".join(
                    [
                        sql_text(one),
                        sql_text(one_name),
                        sql_text(two),
                        sql_text(two_name),
                        sql_text(three),
                        sql_text(three_name),
                        sql_text(four),
                        sql_text(four_name),
                        sql_text(n.code),
                        sql_text(n.name),
                        str(gen_count),
                    ]
                )
                + ")"
            )

    return base_class_rows, class_sku_rows, leaf_seed_rows


def generate_sql() -> str:
    nodes = parse_data()
    base_class_rows, class_sku_rows, leaf_seed_rows = build_rows(nodes)

    parts: list[str] = []
    parts.append("-- mock seed generated from script/data.txt")
    parts.append("SET NOCOUNT ON;")
    parts.append("BEGIN TRAN;")
    parts.append("")

    parts.append("-- store dimension")
    parts.append("DELETE FROM dbo.base_department WHERE store_no IN (N'S001');")
    parts.append(
        "INSERT INTO dbo.base_department "
        "(store_no,store_name,store_format_no,store_format_name,business_circle_no,business_circle_name,"
        "store_group_no,store_group_name,store_type_no,store_type_name,preorgcode,preorgname,status_no,status_name,"
        "store_staff_num,store_area,longitude,latitude,start_date,end_date)"
    )
    parts.append(
        "VALUES "
        "(N'S001',N'STORE-A',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'1',N'STORE',"
        "N'ORG01',N'AREA-1',1,N'OPEN',N'80',N'2600',N'121.47',N'31.23','2022-01-01',NULL);"
    )
    parts.append("")

    parts.append("-- class hierarchy")
    parts.append("DELETE FROM dbo.base_class;")
    parts.append(
        "INSERT INTO dbo.base_class "
        "(one_class_no,one_class_name,two_class_no,two_class_name,three_class_no,three_class_name,"
        "four_class_no,four_class_name,five_class_no,five_class_name) VALUES"
    )
    parts.append(",\n".join(base_class_rows) + ";")
    parts.append("")

    parts.append("-- suggest sku and role")
    parts.append("DELETE FROM dbo.base_class_sku WHERE store_no = 'S001';")
    parts.append(
        "INSERT INTO dbo.base_class_sku "
        "(store_no,store_name,store_org_no,store_org_name,store_format_no,store_format_name,"
        "business_circle_no,business_circle_name,store_group_no,store_group_name,class_no,class_name,class_sku,class_role) VALUES"
    )
    parts.append(",\n".join(class_sku_rows) + ";")
    parts.append("")

    parts.append("-- demo products for saleSku calculation")
    parts.append("DELETE FROM dbo.base_dept_products WHERE store_no = N'S001';")
    parts.append(
        "DECLARE @seed TABLE ("
        "one_class_no NVARCHAR(40),one_class_name NVARCHAR(40),"
        "two_class_no NVARCHAR(40),two_class_name NVARCHAR(40),"
        "three_class_no NVARCHAR(40),three_class_name NVARCHAR(40),"
        "four_class_no NVARCHAR(40),four_class_name NVARCHAR(40),"
        "five_class_no NVARCHAR(40),five_class_name NVARCHAR(40),"
        "gen_cnt INT"
        ");"
    )
    parts.append(
        "INSERT INTO @seed "
        "(one_class_no,one_class_name,two_class_no,two_class_name,three_class_no,three_class_name,"
        "four_class_no,four_class_name,five_class_no,five_class_name,gen_cnt) VALUES"
    )
    parts.append(",\n".join(leaf_seed_rows) + ";")
    parts.append(
        "WITH N AS ("
        "SELECT TOP (200) ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS n "
        "FROM sys.all_objects"
        ")"
    )
    parts.append(
        "INSERT INTO dbo.base_dept_products ("
        "store_no,store_name,one_class_no,one_class_name,two_class_no,two_class_name,"
        "three_class_no,three_class_name,four_class_no,four_class_name,five_class_no,five_class_name,"
        "div_no,div_name,subdiv_no,subdiv_name,bclass_no,bclass_name,mclass_no,mclass_name,sclass_no,sclass_name,"
        "product_no,product_barcode,product_name,product_taxin_inprice,product_saleprice,status_no,status_name,sale_type_no,sale_type,updatetime"
        ")"
    )
    parts.append(
        "SELECT "
        "N'S001',N'STORE-A',"
        "s.one_class_no,s.one_class_name,s.two_class_no,s.two_class_name,s.three_class_no,s.three_class_name,"
        "s.four_class_no,s.four_class_name,s.five_class_no,s.five_class_name,"
        "N'D01',N'DEP-1',N'D02',N'DEP-2',N'D03',N'DEP-3',N'D04',N'DEP-4',N'D05',N'DEP-5',"
        "CONCAT('P',s.five_class_no,RIGHT(CONCAT('000',CAST(n.n AS VARCHAR(3))),3)),"
        "CONCAT('B',s.five_class_no,RIGHT(CONCAT('000',CAST(n.n AS VARCHAR(3))),3)),"
        "CONCAT(s.five_class_name,N'-',n.n),"
        "CAST(5 + (n.n % 30) AS DECIMAL(12,2)),"
        "CAST(8 + (n.n % 30) AS DECIMAL(12,2)),"
        "1,N'NORMAL',1,N'SELF',GETDATE() "
        "FROM @seed s "
        "JOIN N ON N.n <= s.gen_cnt;"
    )
    parts.append("")
    parts.append("-- quick check")
    parts.append("SELECT COUNT(1) AS base_class_cnt FROM dbo.base_class;")
    parts.append("SELECT COUNT(1) AS base_class_sku_cnt FROM dbo.base_class_sku WHERE store_no='S001';")
    parts.append("SELECT COUNT(1) AS base_dept_products_cnt FROM dbo.base_dept_products WHERE store_no='S001';")
    parts.append("")
    parts.append("COMMIT TRAN;")
    parts.append("")

    return "\n".join(parts)


def main() -> None:
    sql = generate_sql()
    OUT_FILE.parent.mkdir(parents=True, exist_ok=True)
    OUT_FILE.write_text(sql, encoding="utf-8")
    print(f"generated: {OUT_FILE}")
    print(f"size: {len(sql)} chars")


if __name__ == "__main__":
    main()
