package org.ascnet.leaftown.client;

public enum CashInventoryType 
{
    EQUIP(0x01),
    USE(0x02),
    SETUP(0x03),
    ETC(0x04),
    CASH(0x05);

    final byte type;

    private CashInventoryType(int type) 
    {
        this.type = (byte) type;
    }

    public byte getType()
    {
        return type;
    }

    public short getBitfieldEncoding()
    {
        return (short) (0x02 << type);
    }

    public static MapleStorageType getByType(byte type) 
    {
        for (MapleStorageType l : MapleStorageType.values())
        {
            if (l.type == type) 
                return l;
        }
        return null;
    }
}