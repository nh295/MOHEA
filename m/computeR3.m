function R3 = computeR3(setA,setB,refpoint,vectors)

R3 = 0;
for i=1:length(vectors)
    Butil = popUtility(setB,refpoint,vectors(i,:));
    R3 = R3 + (Butil- popUtility(setA,refpoint,vectors(i,:)))/Butil;
    %disp(R2)
end
R3 = R3 / length(vectors);

figure(1)
scatter(setA(:,1),setA(:,2),'o')
hold on
scatter(setB(:,1),setB(:,2),'s','filled')
hold off
legend('setA','setB')
end

function u = popUtility(pop, refpoint,vector)
utilities = zeros(length(pop),1);
for i=1:length(pop)
    utilities(i) = solnUtility(pop(i,:),refpoint,vector);
end
u = max(utilities);
end

function u = solnUtility(soln, refpoint,vector)
utilities = zeros(length(vector),1);
for i=1:length(vector)
    utilities(i) = vector(i)*abs(soln(i)-refpoint(i));
end
u = -max(utilities);
end

