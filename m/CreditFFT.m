function CreditFFT(filename)

%code to load in csv of credits and conduct fast fourier transform to see
%if there is some clear signal or if it is noise
fid = fopen(filename,'r');
labels = {};
%load in rows one by one
while(feof(fid)==0)
    raw_iteration = strsplit(fgetl(fid),',');
    raw_credits = strsplit(fgetl(fid),',');
    labels = [labels,raw_credits{1}];
    figure(1)
    hold on
    iteration = zeros(length(raw_iteration)-1,1);
    credits = zeros(length(raw_credits)-1,1);
    for i=1:length(raw_credits)-1
       iteration(i)=str2double(raw_iteration{i+1});
       credits(i)=str2double(raw_credits{i+1});
    end
    scatter(iteration,credits,10)
    
    %conduct FFT on rewards received
%     Y = fft(credits);
%     L = length(credits);
%     P2 = abs(Y/L);
%     P1 = P2(1:L/2+1);
%     P1(2:end-1) = 2*P1(2:end-1);
%     figure (2)
%     hold on
%     plot(P1)
end

fclose(fid);
figure(1)
legend(labels)
hold off
% figure(2)
% legend(labels)
% hold off

