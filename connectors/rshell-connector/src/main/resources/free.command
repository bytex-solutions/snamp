<Command name="free">
    <Description>
        free  displays the total amount of free and used physical and swap mem‐
        ory in the system, as well as the buffers  used  by  the  kernel.   The
        shared  memory column represents either the MemShared value (2.4 series
        kernels) or the Shmem value (2.6 series kernels and later)  taken  from
        the  /proc/meminfo  file.  The  value is zero if none of the entries is
        exported by the kernel.
    </Description>
    <Profile version="*">
        <Input>
            free &lt;
        </Input>
        <Output type="integer" parser="regexp|antlr">

        </Output>
    </Profile>
</Command>