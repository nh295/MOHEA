function vector = random_shuffle(vector)
    global rnduni;
    for i=length(vector):-1:2
        %[r, rnduni]=crandom(rnduni);
        r = rand;
        k=ceil(r*i);
        temp = vector(i);
        vector(i)=vector(k);
        vector(k)=temp;
    end
end
