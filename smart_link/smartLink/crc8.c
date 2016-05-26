static unsigned char SMARTLINK_SlaveCrc8(unsigned char *pBuf, unsigned char len)  
{
    int i;
    unsigned char crc = 0;  

    while(len--)
    {  
        crc = crc^(*pBuf);
        Log.i("andy",*pBuf);
        for(i = 8; i > 0; i--)
        {
            if(crc & 0x80)
            {
                crc = (crc << 1)^0x31;
            }
            else
            {
                crc = crc << 1;
            }
        }
        
        pBuf++;
    }  

    return crc;
}
